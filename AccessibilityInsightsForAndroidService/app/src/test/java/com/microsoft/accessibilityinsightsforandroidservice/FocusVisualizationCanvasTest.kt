// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import android.graphics.Canvas
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FocusVisualizationCanvasTest {
    lateinit var testSubject: FocusVisualizationCanvas

    @Mock
    lateinit var contextMock: Context

    @Mock
    lateinit var focusElementHighlightMock: FocusElementHighlight

    @Mock
    lateinit var focusElementLineMock: FocusElementLine

    @Mock
    lateinit var canvasMock: Canvas

    @Before
    fun prepare() {
        testSubject = FocusVisualizationCanvas(contextMock)
    }

    @Test
    @Throws(Exception::class)
    fun drawHighlightsAndLinesOnlyDrawsHighlightOnFirstPass() {
        val lineStub = ArrayList<FocusElementLine>()
        lineStub.add(focusElementLineMock)

        val highlightStub = ArrayList<FocusElementHighlight>()
        highlightStub.add(focusElementHighlightMock)

        testSubject.setDrawItems(highlightStub, lineStub)
        testSubject.drawHighlightsAndLines(canvasMock)

        Mockito
            .verify(focusElementHighlightMock, Mockito.times(1))
            .drawElementHighlight(ArgumentMatchers.any(Canvas::class.java))
        Mockito
            .verify(focusElementLineMock, Mockito.times(0))
            .drawLine(ArgumentMatchers.any(Canvas::class.java))
    }

    @Test
    @Throws(Exception::class)
    fun drawHighlightsAndLinesDrawsAllRelevantObjectsOnSubsequentPasses() {
        val lineStub = ArrayList<FocusElementLine>()
        lineStub.add(focusElementLineMock)
        lineStub.add(focusElementLineMock)

        val highlightStub = ArrayList<FocusElementHighlight>()
        highlightStub.add(focusElementHighlightMock)
        highlightStub.add(focusElementHighlightMock)

        testSubject.setDrawItems(highlightStub, lineStub)
        testSubject.drawHighlightsAndLines(canvasMock)

        // Note: drawElementHighlight will call twice for each subsequent onDraw event.  This is to
        // ensure that the line is drawn underneath the highlight, as the canvas drawings draw on
        // top of any previous drawings by default.
        Mockito
            .verify(focusElementHighlightMock, Mockito.times(3))
            .drawElementHighlight(ArgumentMatchers.any(Canvas::class.java))
        Mockito
            .verify(focusElementLineMock, Mockito.times(1))
            .drawLine(ArgumentMatchers.any(Canvas::class.java))
    }
}
