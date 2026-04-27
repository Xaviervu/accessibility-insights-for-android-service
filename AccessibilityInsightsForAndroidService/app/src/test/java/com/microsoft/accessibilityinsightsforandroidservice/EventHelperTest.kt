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
class EventHelperTest {
    @Mock
    var mockSwapper: ThreadSafeSwapper<AccessibilityNodeInfo?>? = null

    @Mock
    var mockNodeInfo: AccessibilityNodeInfo? = null

    @Mock
    var mockLastNodeInfo: AccessibilityNodeInfo? = null

    var testSubject: EventHelper? = null

    @Before
    fun prepare() {
        testSubject = EventHelper(mockSwapper!!)
    }

    @Test
    fun eventHelperExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun claimLastSourceReturnsExpectedNodeInfo() {
        Mockito.`when`<AccessibilityNodeInfo?>(mockSwapper!!.swap(null)).thenReturn(mockNodeInfo)

        val actualResponse = testSubject!!.claimLastSource()

        Assert.assertEquals(mockNodeInfo, actualResponse)
    }

    @Test
    fun claimLastSourceCallsSwapWithNullObjectOnlyOnce() {
        testSubject!!.claimLastSource()

        Mockito.verify<ThreadSafeSwapper<AccessibilityNodeInfo?>?>(mockSwapper, Mockito.times(1))
            .swap(null)
    }

    @Test
    fun restoreLastSourceCallsSetIfCurrentlyNullOnlyOnce() {
        testSubject!!.restoreLastSource(mockNodeInfo)

        Mockito.verify<ThreadSafeSwapper<AccessibilityNodeInfo?>?>(mockSwapper, Mockito.times(1))
            .setIfCurrentlyNull(mockNodeInfo)
    }

    @Test
    fun recordEventProperlyHandlesNonNullEventSource() {
        Mockito.`when`<AccessibilityNodeInfo?>(mockSwapper!!.swap(mockNodeInfo))
            .thenReturn(mockLastNodeInfo)

        testSubject!!.recordEvent(mockNodeInfo)

        Mockito.verify<AccessibilityNodeInfo?>(mockLastNodeInfo, Mockito.times(1)).recycle()
    }

    @Test
    fun recordEventProperlyHandlesNullEventSource() {
        testSubject!!.recordEvent(null)

        Mockito.verify<ThreadSafeSwapper<AccessibilityNodeInfo?>?>(mockSwapper, Mockito.times(0))
            .swap(mockNodeInfo)
        Mockito.verify<AccessibilityNodeInfo?>(mockLastNodeInfo, Mockito.times(0)).recycle()
    }

    @Test
    fun recordEventProperlyHandlesNullLastSource() {
        Mockito.`when`<AccessibilityNodeInfo?>(mockSwapper!!.swap(mockNodeInfo)).thenReturn(null)

        testSubject!!.recordEvent(mockNodeInfo)

        Mockito.verify<AccessibilityNodeInfo?>(mockLastNodeInfo, Mockito.times(0)).recycle()
    }
}
