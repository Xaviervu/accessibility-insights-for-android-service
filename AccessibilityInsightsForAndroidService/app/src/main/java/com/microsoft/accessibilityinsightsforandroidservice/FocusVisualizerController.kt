// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Date
import java.util.function.Consumer

class FocusVisualizerController(
    private val focusVisualizer: FocusVisualizer,
    private val focusVisualizationStateManager: FocusVisualizationStateManager,
    private val uiThreadRunner: UIThreadRunner,
    private val windowManager: WindowManager,
    private val layoutParamGenerator: LayoutParamGenerator,
    private val focusVisualizationCanvas: FocusVisualizationCanvas?,
    private val dateProvider: DateProvider,
) {
    private var lastEventSource: AccessibilityNodeInfo? = null
    private var lastOrientationChange: Date
    private val maximumOrientationChangeDelay: Long = 1000

    init {
        this.focusVisualizationStateManager.subscribe(
            Consumer { enabled: Boolean? ->
                this.onFocusVisualizationStateChange(
                    enabled!!,
                )
            },
        )
        this.lastOrientationChange = dateProvider.get()
    }

    fun onFocusEvent(event: AccessibilityEvent) {
        lastEventSource = event.source
        if (!focusVisualizationStateManager.state || ignoreFocusEventDueToRecentOrientationChange()) {
            return
        }

        focusVisualizer.addNewFocusedElement(event.source)
    }

    fun onRedrawEvent(event: AccessibilityEvent?) {
        if (!focusVisualizationStateManager.state) {
            return
        }

        focusVisualizer.refreshHighlights()
    }

    fun onAppChanged(nodeInfo: AccessibilityNodeInfo?) {
        if (!focusVisualizationStateManager.state) {
            return
        }

        focusVisualizer.resetVisualizations()
    }

    fun onOrientationChanged(orientation: Int?) {
        if (!focusVisualizationStateManager.state) {
            return
        }
        lastOrientationChange = dateProvider.get()
        windowManager.updateViewLayout(focusVisualizationCanvas, layoutParamGenerator.get())
        focusVisualizer.resetVisualizations()
    }

    private fun onFocusVisualizationStateChange(enabled: Boolean) {
        if (enabled) {
            uiThreadRunner.run(Runnable { this.addFocusVisualizationToScreen() })
        } else {
            uiThreadRunner.run(Runnable { this.removeFocusVisualizationToScreen() })
        }
    }

    private fun addFocusVisualizationToScreen() {
        if (lastEventSource != null) {
            focusVisualizer.addNewFocusedElement(lastEventSource)
        }
        windowManager.addView(focusVisualizationCanvas, layoutParamGenerator.get())
    }

    private fun removeFocusVisualizationToScreen() {
        focusVisualizer.resetVisualizations()
        windowManager.removeView(focusVisualizationCanvas)
    }

    private fun ignoreFocusEventDueToRecentOrientationChange(): Boolean {
        val currentTime = dateProvider.get()
        val cur = currentTime.time
        val last = lastOrientationChange.time
        val timeSinceLastOrientationChange = cur - last
        return timeSinceLastOrientationChange < maximumOrientationChangeDelay
    }
}
