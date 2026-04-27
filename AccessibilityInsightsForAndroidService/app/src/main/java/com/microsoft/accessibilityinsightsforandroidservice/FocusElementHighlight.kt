// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo

class FocusElementHighlight(
    @JvmField val eventSource: AccessibilityNodeInfo,
    private var paints: HashMap<String, Paint>,
    private val radius: Int,
    private val tabStopCount: Int,
    private val view: View
) {
    private var yOffset = 0
    private var xCoordinate = 0
    private var yCoordinate = 0
    private val rect: Rect
    private var isCurrentElement = true

    init {
        this.rect = Rect()
    }

    private fun setCoordinates() {
        this.eventSource!!.getBoundsInScreen(this.rect)
        this.rect.offset(0, this.yOffset)
        this.xCoordinate = rect.centerX()
        this.yCoordinate = rect.centerY()
    }

    fun drawElementHighlight(canvas: Canvas) {
        if (this.eventSource == null) {
            return
        }

        if (!this.eventSource.refresh()) {
            return
        }

        this.updateWithNewCoordinates()

        if (isCurrentElement) {
            this.drawInnerCircle(
                this.xCoordinate,
                this.yCoordinate,
                this.radius,
                this.paints.get("transparentInnerCircle")!!,
                canvas
            )
        } else {
            this.drawInnerCircle(
                this.xCoordinate,
                this.yCoordinate,
                this.radius,
                this.paints.get("innerCircle")!!,
                canvas
            )
            this.drawNumberInCircle(
                this.xCoordinate,
                this.yCoordinate,
                this.tabStopCount,
                this.paints.get("number")!!,
                canvas
            )
        }

        this.drawOuterCircle(
            this.xCoordinate,
            this.yCoordinate,
            this.radius,
            this.paints.get("outerCircle")!!,
            canvas
        )
    }

    private fun drawInnerCircle(
        xCoordinate: Int, yCoordinate: Int, radius: Int, paint: Paint, canvas: Canvas
    ) {
        canvas.drawCircle(xCoordinate.toFloat(), yCoordinate.toFloat(), radius.toFloat(), paint)
    }

    private fun drawOuterCircle(
        xCoordinate: Int, yCoordinate: Int, radius: Int, paint: Paint, canvas: Canvas
    ) {
        canvas.drawCircle(
            xCoordinate.toFloat(),
            yCoordinate.toFloat(),
            (radius + 3).toFloat(),
            paint
        )
    }

    private fun drawNumberInCircle(
        xCoordinate: Int, yCoordinate: Int, tabStopCount: Int, paint: Paint, canvas: Canvas
    ) {
        canvas.drawText(
            tabStopCount.toString(),
            xCoordinate.toFloat(),
            yCoordinate - ((paint.descent() + paint.ascent()) / 2),
            paint
        )
    }

    fun setPaints(paints: HashMap<String, Paint>) {
        this.paints = paints
    }

    fun setAsNonCurrentElement() {
        this.isCurrentElement = false
    }

    private fun updateWithNewCoordinates() {
        this.yOffset = OffsetHelper.getYOffset(this.view)
        this.setCoordinates()
    }

    companion object {
        private const val TAG = "FocusElementHighlight"
    }
}
