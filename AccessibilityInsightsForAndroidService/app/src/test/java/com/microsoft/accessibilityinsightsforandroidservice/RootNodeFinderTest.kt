// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RootNodeFinderTest {
    @Mock
    var sourceMock: AccessibilityNodeInfo? = null

    @Mock
    var parentMock: AccessibilityNodeInfo? = null

    @Mock
    var grandparentMock: AccessibilityNodeInfo? = null

    var testSubject: RootNodeFinder? = null

    @Before
    fun prepare() {
        testSubject = RootNodeFinder()
    }

    @Test
    fun returnNullIfSourceIsNull() {
        Assert.assertNull(testSubject!!.getRootNodeFromSource(null))
    }

    @Test
    fun rootNodeExistsIfSourceExists() {
        Assert.assertNotNull(testSubject!!.getRootNodeFromSource(sourceMock))
    }

    @Test
    fun rootNodeIsSource() {
        val rootNode = testSubject!!.getRootNodeFromSource(sourceMock)
        Assert.assertEquals(rootNode, sourceMock)
    }

    @Test
    fun rootNodeIsSourceParent() {
        Mockito.`when`<AccessibilityNodeInfo?>(sourceMock!!.getParent()).thenReturn(parentMock)

        val rootNode = testSubject!!.getRootNodeFromSource(sourceMock)
        Assert.assertEquals(rootNode, parentMock)
    }

    @Test
    fun rootNodeIsSourceAncestor() {
        Mockito.`when`<AccessibilityNodeInfo?>(sourceMock!!.getParent()).thenReturn(parentMock)
        Mockito.`when`<AccessibilityNodeInfo?>(parentMock!!.getParent()).thenReturn(grandparentMock)

        val rootNode = testSubject!!.getRootNodeFromSource(sourceMock)
        Assert.assertEquals(rootNode, grandparentMock)
    }

    @Test
    fun uneededNodesGetRecycled() {
        Mockito.`when`<AccessibilityNodeInfo?>(sourceMock!!.getParent()).thenReturn(parentMock)
        Mockito.`when`<AccessibilityNodeInfo?>(parentMock!!.getParent()).thenReturn(grandparentMock)

        val rootNode = testSubject!!.getRootNodeFromSource(sourceMock)
        Mockito.verify<AccessibilityNodeInfo?>(sourceMock, Mockito.never()).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(rootNode, Mockito.never()).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(parentMock, Mockito.times(1)).recycle()
    }
}
