// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.function.Consumer

@RunWith(MockitoJUnitRunner::class)
class AccessibilityEventDispatcherTest {
    @Mock
    var eventMock: AccessibilityEvent? = null

    @Mock
    var rootNodeMock: AccessibilityNodeInfo? = null

    @Mock
    var onAppChangedListenerMock: Consumer<AccessibilityNodeInfo?>? = null

    @Mock
    var onFocusEventListenerMock: Consumer<AccessibilityEvent?>? = null

    @Mock
    var onRedrawEventListenerMock: Consumer<AccessibilityEvent?>? = null

    var testSubject: AccessibilityEventDispatcher? = null

    @Before
    fun prepare() {
        val packageNameStub: CharSequence = "some package name"
        Mockito.`when`<CharSequence?>(rootNodeMock!!.getPackageName()).thenReturn(packageNameStub)

        testSubject = AccessibilityEventDispatcher()
    }

    @Test
    fun accessibilityEventDispatcherExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun onAppChangedFiresWithoutPreviousPackageName() {
        val trivialEventType = -1
        Mockito.`when`<Int?>(eventMock!!.getEventType()).thenReturn(trivialEventType)

        testSubject!!.addOnAppChangedListener(onAppChangedListenerMock)
        testSubject!!.onAccessibilityEvent(eventMock!!, rootNodeMock)

        Mockito.verify<Consumer<AccessibilityNodeInfo?>?>(
            onAppChangedListenerMock,
            Mockito.times(1)
        ).accept(rootNodeMock)
    }

    @Test
    fun onAppChangedDoesNotFireWhenRootNodeIsNull() {
        val trivialEventType = -1
        Mockito.`when`<Int?>(eventMock!!.getEventType()).thenReturn(trivialEventType)

        testSubject!!.addOnAppChangedListener(onAppChangedListenerMock)
        testSubject!!.onAccessibilityEvent(eventMock!!, null)

        Mockito.verify<Consumer<AccessibilityNodeInfo?>?>(
            onAppChangedListenerMock,
            Mockito.times(0)
        ).accept(rootNodeMock)
    }

    @Test
    fun onAppChangedFiresWhenPackageNameChanged() {
        val trivialEventType = -1
        val differentPackageNameStub: CharSequence = "different package name"
        Mockito.`when`<Int?>(eventMock!!.getEventType()).thenReturn(trivialEventType)

        testSubject!!.addOnAppChangedListener(onAppChangedListenerMock)
        testSubject!!.onAccessibilityEvent(eventMock!!, rootNodeMock)

        Mockito.reset<AccessibilityNodeInfo?>(rootNodeMock)

        Mockito.`when`<CharSequence?>(rootNodeMock!!.getPackageName())
            .thenReturn(differentPackageNameStub)
        testSubject!!.onAccessibilityEvent(eventMock!!, rootNodeMock)

        Mockito.verify<Consumer<AccessibilityNodeInfo?>?>(
            onAppChangedListenerMock,
            Mockito.times(2)
        ).accept(rootNodeMock)
    }

    @Test
    fun onFocusEventListenerFiresOnFocusEvent() {
        val focusEventType = AccessibilityEvent.TYPE_VIEW_FOCUSED
        Mockito.`when`<Int?>(eventMock!!.getEventType()).thenReturn(focusEventType)

        testSubject!!.addOnFocusEventListener(onFocusEventListenerMock)
        testSubject!!.onAccessibilityEvent(eventMock!!, rootNodeMock)

        Mockito.verify<Consumer<AccessibilityEvent?>?>(onFocusEventListenerMock, Mockito.times(1))
            .accept(eventMock)
    }

    @Test
    fun onFocusEventListenerDoesNotFiresOnOtherEvent() {
        val trivialEventType = -1
        Mockito.`when`<Int?>(eventMock!!.getEventType()).thenReturn(trivialEventType)

        testSubject!!.addOnFocusEventListener(onFocusEventListenerMock)
        testSubject!!.onAccessibilityEvent(eventMock!!, rootNodeMock)

        Mockito.verify<Consumer<AccessibilityEvent?>?>(onFocusEventListenerMock, Mockito.times(0))
            .accept(eventMock)
    }

    @Test
    fun onRedrawEventListenerFiresOnRedrawEvents() {
        testSubject!!.addOnRedrawEventListener(onRedrawEventListenerMock)

        AccessibilityEventDispatcher.redrawEventTypes.forEach(
            Consumer { eventType: Int? ->
                Mockito.`when`<Int?>(eventMock!!.getEventType()).thenReturn(eventType)
                testSubject!!.onAccessibilityEvent(eventMock!!, rootNodeMock)
                Mockito.reset<AccessibilityEvent?>(eventMock)
            })

        Mockito.verify<Consumer<AccessibilityEvent?>?>(onRedrawEventListenerMock, Mockito.times(4))
            .accept(eventMock)
    }

    @Test
    fun onRedrawEventListenerDoesNotFiresOnOtherEvent() {
        val trivialEventType = -1
        Mockito.`when`<Int?>(eventMock!!.getEventType()).thenReturn(trivialEventType)

        testSubject!!.addOnRedrawEventListener(onRedrawEventListenerMock)
        testSubject!!.onAccessibilityEvent(eventMock!!, rootNodeMock)

        Mockito.verify<Consumer<AccessibilityEvent?>?>(onRedrawEventListenerMock, Mockito.times(0))
            .accept(eventMock)
    }
}
