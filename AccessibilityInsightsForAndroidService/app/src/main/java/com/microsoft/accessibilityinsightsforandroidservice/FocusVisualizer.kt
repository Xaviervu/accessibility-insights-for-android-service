// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo

class FocusVisualizer(
    private val styles: FocusVisualizerStyles,
    private val focusVisualizationCanvas: FocusVisualizationCanvas,
) {
    private val focusElementHighlights: ArrayList<FocusElementHighlight> = ArrayList<FocusElementHighlight>()
    private val focusElementLines: ArrayList<FocusElementLine> = ArrayList<FocusElementLine>()
    private var tabStopCount = 0

    fun refreshHighlights() {
        this.focusVisualizationCanvas.redraw()
    }

    fun addNewFocusedElement(eventSource: AccessibilityNodeInfo?) {
        if (eventSource == null) return
        tabStopCount++

        val previousEventSource = this.previousEventSource

        if (this.focusElementHighlights.isNotEmpty()) {
            this.setPreviousElementHighlightNonCurrent(
                this.focusElementHighlights[this.focusElementHighlights.size - 1],
            )
        }
        if (focusElementLines.isNotEmpty()) {
            this.setPreviousLineNonCurrent(this.focusElementLines[this.focusElementLines.size - 1])
        }

        this.createFocusElementHighlight(eventSource)
        this.createFocusElementLine(eventSource, previousEventSource)

        this.setDrawItemsAndRedraw()
    }

    fun resetVisualizations() {
        this.tabStopCount = 0
        this.focusElementHighlights.clear()
        this.focusElementLines.clear()
        this.setDrawItemsAndRedraw()
    }

    private fun setPreviousLineNonCurrent(line: FocusElementLine) {
        line.setPaint(this.styles.nonCurrentLinePaints)
    }

    private fun setPreviousElementHighlightNonCurrent(focusElementHighlight: FocusElementHighlight) {
        focusElementHighlight.setAsNonCurrentElement()
        focusElementHighlight.setPaints(this.styles.nonCurrentElementPaints)
    }

    private fun createFocusElementLine(
        eventSource: AccessibilityNodeInfo?,
        previousEventSource: AccessibilityNodeInfo?,
    ) {
        val focusElementLine =
            FocusElementLine(
                eventSource,
                previousEventSource,
                this.styles.currentLinePaints,
                this.focusVisualizationCanvas,
            )
        this.focusElementLines.add(focusElementLine)
    }

    private fun createFocusElementHighlight(eventSource: AccessibilityNodeInfo) {
        val focusElementHighlight =
            FocusElementHighlight(
                eventSource,
                this.styles.currentElementPaints,
                this.styles.focusElementHighlightRadius,
                this.tabStopCount,
                this.focusVisualizationCanvas,
            )
        this.focusElementHighlights.add(focusElementHighlight)
    }

    private val previousEventSource: AccessibilityNodeInfo?
        get() {
            if (this.focusElementHighlights.isEmpty()) {
                return null
            }
            return this.focusElementHighlights[this.focusElementHighlights.size - 1]
                .eventSource
        }

    private fun setDrawItemsAndRedraw() {
        this.focusVisualizationCanvas.setDrawItems(
            this.focusElementHighlights,
            this.focusElementLines,
        )
        this.focusVisualizationCanvas.redraw()
    }
}
