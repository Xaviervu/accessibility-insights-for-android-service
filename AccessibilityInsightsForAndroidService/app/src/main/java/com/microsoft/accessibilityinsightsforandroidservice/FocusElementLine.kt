// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo

class FocusElementLine(
    private val eventSource: AccessibilityNodeInfo?,
    private val previousEventSource: AccessibilityNodeInfo?,
    private var paints: HashMap<String, Paint>,
    private val view: View,
) {
    lateinit var paintVar: HashMap<String, Paint>
    private var yOffset = 0
    private var xStart = 0
    private var yStart = 0
    private var xEnd = 0
    private var yEnd = 0
    private val currentRect: Rect
    private val prevRect: Rect

    init {
        this.currentRect = Rect()
        this.prevRect = Rect()
    }

    fun drawLine(canvas: Canvas) {
        if (this.eventSource == null || this.previousEventSource == null) {
            return
        }

        if (!this.eventSource.refresh() || !this.previousEventSource.refresh()) {
            return
        }

        this.updateWithNewCoordinates()
        this.drawConnectingLine(
            this.xStart,
            this.yStart,
            this.xEnd,
            this.yEnd,
            this.paints.get("backgroundLine")!!,
            canvas,
        )
        this.drawConnectingLine(
            this.xStart,
            this.yStart,
            this.xEnd,
            this.yEnd,
            this.paints.get("foregroundLine")!!,
            canvas,
        )
    }

    private fun setCoordinates() {
        this.eventSource!!.getBoundsInScreen(this.currentRect)
        this.currentRect.offset(0, this.yOffset)

        this.previousEventSource!!.getBoundsInScreen(this.prevRect)
        this.prevRect.offset(0, this.yOffset)

        this.xStart = currentRect.centerX()
        this.yStart = currentRect.centerY()
        this.xEnd = prevRect.centerX()
        this.yEnd = prevRect.centerY()
    }

    private fun drawConnectingLine(
        xStart: Int,
        yStart: Int,
        xEnd: Int,
        yEnd: Int,
        paint: Paint,
        canvas: Canvas,
    ) {
        canvas.drawLine(xStart.toFloat(), yStart.toFloat(), xEnd.toFloat(), yEnd.toFloat(), paint)
    }

    fun setPaint(paints: HashMap<String, Paint>) {
        this.paints = paints
    }

    private fun updateWithNewCoordinates() {
        this.yOffset = OffsetHelper.getYOffset(this.view)
        this.setCoordinates()
    }
}
