// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeView
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NodeViewBuilderFactoryTest {
    @Mock
    lateinit var node: AccessibilityNodeInfo

    @Mock
    var view: AxeView? = null

    lateinit var testSubject: NodeViewBuilderFactory

    @Before
    fun prepare() {
        testSubject = NodeViewBuilderFactory()
    }

    @Test
    fun nodeViewIsNotNull() {
        Assert.assertNotNull(testSubject.createNodeViewBuilder(node, mutableListOf(), null))
    }
}
