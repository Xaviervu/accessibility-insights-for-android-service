// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer
import java.util.Date
import java.util.function.Consumer

@RunWith(MockitoJUnitRunner::class)
class FocusVisualizerControllerTest {
    @Mock
    var focusVisualizerMock: FocusVisualizer? = null

    @Mock
    var focusVisualizationStateManagerMock: FocusVisualizationStateManager? = null

    @Mock
    var accessibilityEventMock: AccessibilityEvent? = null

    @Mock
    var uiThreadRunner: UIThreadRunner? = null

    @Mock
    var windowManager: WindowManager? = null

    @Mock
    var layoutParamGenerator: LayoutParamGenerator? = null

    @Mock
    var focusVisualizationCanvas: FocusVisualizationCanvas? = null

    @Mock
    var layoutParams: WindowManager.LayoutParams? = null

    @Mock
    var accessibilityNodeInfo: AccessibilityNodeInfo? = null

    @Mock
    var dateProvider: DateProvider? = null

    @Mock
    var oldDateMock: Date? = null

    @Mock
    var newDateMock: Date? = null

    var listener: Consumer<Boolean?>? = null
    var testSubject: FocusVisualizerController? = null

    @Before
    fun prepare() {
        Mockito
            .`when`<WindowManager.LayoutParams>(layoutParamGenerator!!.get())
            .thenReturn(layoutParams)
        listener = null
        Mockito.`when`<Date>(dateProvider!!.get()).thenReturn(oldDateMock)
        testSubject =
            FocusVisualizerController(
                focusVisualizerMock!!,
                focusVisualizationStateManagerMock!!,
                uiThreadRunner!!,
                windowManager!!,
                layoutParamGenerator!!,
                focusVisualizationCanvas,
                dateProvider!!,
            )
    }

    @Test
    fun exists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun onFocusEventDoesNotCallVisualizerIfStateIsFalse() {
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(false)
        testSubject!!.onFocusEvent(accessibilityEventMock!!)
        Mockito
            .verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(0))
            .addNewFocusedElement(accessibilityNodeInfo)
    }

    @Test
    fun onFocusEventDoesNotCallVisualizerIfOrientationChangedRecently() {
        Mockito.reset<DateProvider?>(dateProvider)
        Mockito.`when`<Date>(dateProvider!!.get()).thenReturn(newDateMock)
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(true)
        Mockito.`when`<Long?>(oldDateMock!!.getTime()).thenReturn(500L)
        Mockito.`when`<Long?>(newDateMock!!.getTime()).thenReturn(501L)
        testSubject!!.onFocusEvent(accessibilityEventMock!!)
        Mockito
            .verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(0))
            .addNewFocusedElement(accessibilityNodeInfo)
    }

    @Test
    @Throws(Exception::class)
    fun onFocusEventCallsVisualizerIfStateIsTrueAndOrientationHasNotChangedRecently() {
        Mockito.reset<DateProvider?>(dateProvider)
        Mockito.`when`<Date>(dateProvider!!.get()).thenReturn(newDateMock)
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(true)
        Mockito
            .`when`<AccessibilityNodeInfo?>(accessibilityEventMock!!.getSource())
            .thenReturn(accessibilityNodeInfo)
        Mockito.`when`<Long?>(oldDateMock!!.getTime()).thenReturn(500L)
        Mockito.`when`<Long?>(newDateMock!!.getTime()).thenReturn(10000L)
        testSubject!!.onFocusEvent(accessibilityEventMock!!)
        Mockito
            .verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(1))
            .addNewFocusedElement(accessibilityNodeInfo)
    }

    @Test
    fun onRedrawEventDoesNotCallVisualizerIfStateIsFalse() {
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(false)
        testSubject!!.onRedrawEvent(accessibilityEventMock)
        Mockito.verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(0)).refreshHighlights()
    }

    @Test
    fun onRedrawEventCallsVisualizerIfStateIsTrue() {
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(true)
        testSubject!!.onRedrawEvent(accessibilityEventMock)
        Mockito.verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(1)).refreshHighlights()
    }

    @Test
    fun onAppChangeDoesNotCallVisualizerIfStateIsFalse() {
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(false)
        testSubject!!.onAppChanged(accessibilityNodeInfo)
        Mockito
            .verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(0))
            .resetVisualizations()
    }

    @Test
    fun onAppChangeDoesCallVisualizerIfStateIsTrue() {
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(true)
        testSubject!!.onAppChanged(accessibilityNodeInfo)
        Mockito
            .verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(1))
            .resetVisualizations()
    }

    @Test
    fun onOrientationChangeDoesNothingIfStateIsFalse() {
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(false)
        testSubject!!.onOrientationChanged(0)
        Mockito
            .verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(0))
            .resetVisualizations()
        Mockito
            .verify<WindowManager?>(windowManager, Mockito.times(0))
            .updateViewLayout(focusVisualizationCanvas, layoutParams)
    }

    @Test
    fun onOrientationChangeUpdatesVisualizationAsNecessaryIfStateIsTrue() {
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(true)
        testSubject!!.onOrientationChanged(0)
        Mockito
            .verify<FocusVisualizer?>(focusVisualizerMock, Mockito.times(1))
            .resetVisualizations()
        Mockito
            .verify<WindowManager?>(windowManager, Mockito.times(1))
            .updateViewLayout(focusVisualizationCanvas, layoutParams)
    }

    @Test
    fun onFocusVisualizationStateChangeToEnabledAddsVisualization() {
        Mockito
            .doAnswer(
                Answer { invocation: InvocationOnMock? ->
                    listener = invocation!!.getArgument<Consumer<Boolean?>?>(0)
                    listener!!.accept(true)
                    null
                },
            ).`when`<FocusVisualizationStateManager?>(focusVisualizationStateManagerMock)
            .subscribe(ArgumentMatchers.any<Consumer<Boolean?>?>())

        Mockito
            .doAnswer(
                Answer { invocation: InvocationOnMock? ->
                    val runnable = invocation!!.getArgument<Runnable>(0)
                    runnable.run()
                    null
                },
            ).`when`<UIThreadRunner?>(uiThreadRunner)
            .run(ArgumentMatchers.any<Runnable>())

        testSubject =
            FocusVisualizerController(
                focusVisualizerMock!!,
                focusVisualizationStateManagerMock!!,
                uiThreadRunner!!,
                windowManager!!,
                layoutParamGenerator!!,
                focusVisualizationCanvas,
                dateProvider!!,
            )

        Mockito
            .verify<WindowManager?>(windowManager)
            .addView(focusVisualizationCanvas, layoutParams)
    }

    @Test
    fun onFocusVisualizationStateChangeToEnabledAddsVisualizationWithLastEventSource() {
        Mockito.`when`<Boolean?>(focusVisualizationStateManagerMock!!.state).thenReturn(false)
        Mockito
            .`when`<AccessibilityNodeInfo?>(accessibilityEventMock!!.getSource())
            .thenReturn(accessibilityNodeInfo)
        Mockito
            .doAnswer(
                Answer { invocation: InvocationOnMock? ->
                    listener = invocation!!.getArgument<Consumer<Boolean?>?>(0)
                    null
                },
            ).`when`<FocusVisualizationStateManager?>(focusVisualizationStateManagerMock)
            .subscribe(ArgumentMatchers.any<Consumer<Boolean?>?>())

        Mockito
            .doAnswer(
                Answer { invocation: InvocationOnMock? ->
                    val runnable = invocation!!.getArgument<Runnable>(0)
                    runnable.run()
                    null
                },
            ).`when`<UIThreadRunner?>(uiThreadRunner)
            .run(ArgumentMatchers.any<Runnable>())

        testSubject =
            FocusVisualizerController(
                focusVisualizerMock!!,
                focusVisualizationStateManagerMock!!,
                uiThreadRunner!!,
                windowManager!!,
                layoutParamGenerator!!,
                focusVisualizationCanvas,
                dateProvider!!,
            )

        testSubject!!.onFocusEvent(accessibilityEventMock!!)
        listener!!.accept(true)

        Mockito
            .verify<WindowManager?>(windowManager)
            .addView(focusVisualizationCanvas, layoutParams)
        Mockito
            .verify<FocusVisualizer?>(focusVisualizerMock)
            .addNewFocusedElement(accessibilityNodeInfo)
    }

    @Test
    fun onFocusVisualizationStateChangToDisabledRemovesVisualizations() {
        Mockito
            .doAnswer(
                Answer { invocation: InvocationOnMock? ->
                    listener = invocation!!.getArgument<Consumer<Boolean?>?>(0)
                    listener!!.accept(false)
                    null
                },
            ).`when`<FocusVisualizationStateManager?>(focusVisualizationStateManagerMock)
            .subscribe(ArgumentMatchers.any<Consumer<Boolean?>?>())

        Mockito
            .doAnswer(
                Answer { invocation: InvocationOnMock? ->
                    val runnable = invocation!!.getArgument<Runnable>(0)
                    runnable.run()
                    null
                },
            ).`when`<UIThreadRunner?>(uiThreadRunner)
            .run(ArgumentMatchers.any<Runnable>())

        testSubject =
            FocusVisualizerController(
                focusVisualizerMock!!,
                focusVisualizationStateManagerMock!!,
                uiThreadRunner!!,
                windowManager!!,
                layoutParamGenerator!!,
                focusVisualizationCanvas,
                dateProvider!!,
            )

        Mockito.verify<FocusVisualizer?>(focusVisualizerMock).resetVisualizations()
    }
}
