// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import android.graphics.Canvas
import android.view.View
import androidx.annotation.VisibleForTesting

class FocusVisualizationCanvas(context: Context?) : View(context) {
    private  var focusElementHighlights: ArrayList<FocusElementHighlight>? = null
    private lateinit var focusElementLines: ArrayList<FocusElementLine>

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.drawHighlightsAndLines(canvas)
    }

    @VisibleForTesting
    fun drawHighlightsAndLines(canvas: Canvas) {
        if (this.focusElementHighlights == null) {
            return
        }
        focusElementHighlights?.let {
            for (elementIndex in it.indices) {
                if (elementIndex != 0) {
                    this.drawTrailingHighlights(elementIndex, canvas)
                }

                it[elementIndex].drawElementHighlight(canvas)
            }
        }

    }

    private fun drawTrailingHighlights(elementIndex: Int, canvas: Canvas) {
        focusElementLines[elementIndex].drawLine(canvas)
        focusElementHighlights?.get(elementIndex - 1)?.drawElementHighlight(canvas)
    }

    fun setDrawItems(
        highlights: ArrayList<FocusElementHighlight>, lines: ArrayList<FocusElementLine>
    ) {
        this.focusElementHighlights = highlights
        this.focusElementLines = lines
    }

    fun redraw() {
        this.invalidate()
    }
}
