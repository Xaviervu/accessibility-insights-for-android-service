// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

class FocusVisualizerStyles {
    private lateinit var currentLinePaint: Paint
    private lateinit var currentOuterCirclePaint: Paint
    private lateinit var nonCurrentLinePaint: Paint
    private lateinit var nonCurrentOuterCirclePaint: Paint
    private lateinit var innerCirclePaint: Paint
    private lateinit var numberPaint: Paint
    private lateinit var currentBackgroundLinePaint: Paint
    private lateinit var nonCurrentBackgroundLinePaint: Paint
    private lateinit var transparentInnerCirclePaint: Paint

    lateinit var currentElementPaints: HashMap<String, Paint>
        private set
    lateinit var nonCurrentElementPaints: HashMap<String, Paint>
        private set
    lateinit var currentLinePaints: HashMap<String, Paint>
        private set
    lateinit var nonCurrentLinePaints: HashMap<String, Paint>
        private set

    var focusElementHighlightRadius: Int = 50

    init {
        this.setCurrentOuterCirclePaint()
        this.setInnerCirclePaint()
        this.setNonCurrentLinePaint()
        this.setNumberPaint()
        this.setCurrentLinePaint()
        this.setNonCurrentOuterCirclePaint()
        this.setCurrentBackgroundLinePaint()
        this.setNonCurrentBackgroundLinePaint()
        this.setTransparentInnerCirclePaint()

        this.setCurrentElementPaints()
        this.setNonCurrentElementPaints()
        this.setCurrentLinePaints()
        this.setNonCurrentLinePaints()
    }

    private fun setCurrentElementPaints() {
        this.currentElementPaints = HashMap()
        this.currentElementPaints["outerCircle"] = this.currentOuterCirclePaint
        this.currentElementPaints["innerCircle"] = this.innerCirclePaint
        this.currentElementPaints["number"] = this.numberPaint
        this.currentElementPaints["transparentInnerCircle"] = this.transparentInnerCirclePaint
    }

    private fun setNonCurrentElementPaints() {
        this.nonCurrentElementPaints = HashMap()
        this.nonCurrentElementPaints["outerCircle"] = this.nonCurrentOuterCirclePaint
        this.nonCurrentElementPaints["innerCircle"] = this.innerCirclePaint
        this.nonCurrentElementPaints["number"] = this.numberPaint
    }

    private fun setCurrentLinePaints() {
        this.currentLinePaints = HashMap()
        this.currentLinePaints["foregroundLine"] = this.currentLinePaint
        this.currentLinePaints["backgroundLine"] = this.currentBackgroundLinePaint
    }

    private fun setNonCurrentLinePaints() {
        this.nonCurrentLinePaints = HashMap()
        this.nonCurrentLinePaints["foregroundLine"] = this.nonCurrentLinePaint
        this.nonCurrentLinePaints["backgroundLine"] = this.nonCurrentBackgroundLinePaint
    }

    private fun setNonCurrentLinePaint() {
        this.nonCurrentLinePaint = Paint()
        this.nonCurrentLinePaint.style = Paint.Style.STROKE
        this.nonCurrentLinePaint.setColor(Color.GRAY)
        this.nonCurrentLinePaint.strokeWidth = 5f
    }

    private fun setNonCurrentOuterCirclePaint() {
        this.nonCurrentOuterCirclePaint = Paint()
        this.nonCurrentOuterCirclePaint.style = Paint.Style.STROKE
        this.nonCurrentOuterCirclePaint.setColor(Color.GRAY)
        this.nonCurrentOuterCirclePaint.strokeWidth = 7f
    }

    private fun setCurrentOuterCirclePaint() {
        this.currentOuterCirclePaint = Paint()
        this.currentOuterCirclePaint.style = Paint.Style.STROKE
        this.currentOuterCirclePaint.setColor(Color.parseColor("#B4009E"))
        this.currentOuterCirclePaint.strokeWidth = 7f
    }

    private fun setInnerCirclePaint() {
        this.innerCirclePaint = Paint()
        this.innerCirclePaint.style = Paint.Style.FILL
        this.innerCirclePaint.setColor(Color.WHITE)
    }

    private fun setNumberPaint() {
        this.numberPaint = Paint()
        this.numberPaint.style = Paint.Style.FILL_AND_STROKE
        this.numberPaint.textAlign = Paint.Align.CENTER
        this.numberPaint.setColor(Color.BLACK)
        this.numberPaint.strokeWidth = 2f
        this.numberPaint.textSize = 45f
    }

    private fun setCurrentLinePaint() {
        this.currentLinePaint = Paint()
        this.currentLinePaint.style = Paint.Style.STROKE
        this.currentLinePaint.setColor(Color.parseColor("#B4009E"))
        this.currentLinePaint.strokeWidth = 7f
        this.currentLinePaint.setPathEffect(DashPathEffect(floatArrayOf(20f, 5f), 0f))
    }

    private fun setCurrentBackgroundLinePaint() {
        this.currentBackgroundLinePaint = Paint()
        this.currentBackgroundLinePaint.style = Paint.Style.STROKE
        this.currentBackgroundLinePaint.setColor(Color.WHITE)
        this.currentBackgroundLinePaint.strokeWidth = 12f
        this.currentBackgroundLinePaint.setPathEffect(DashPathEffect(floatArrayOf(24f, 1f), 2f))
    }

    private fun setNonCurrentBackgroundLinePaint() {
        this.nonCurrentBackgroundLinePaint = Paint()
        this.nonCurrentBackgroundLinePaint.style = Paint.Style.STROKE
        this.nonCurrentBackgroundLinePaint.setColor(Color.WHITE)
        this.nonCurrentBackgroundLinePaint.strokeWidth = 12f
    }

    private fun setTransparentInnerCirclePaint() {
        this.transparentInnerCirclePaint = Paint()
        this.transparentInnerCirclePaint.style = Paint.Style.FILL
        this.transparentInnerCirclePaint.setColor(Color.TRANSPARENT)
        this.transparentInnerCirclePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
}
