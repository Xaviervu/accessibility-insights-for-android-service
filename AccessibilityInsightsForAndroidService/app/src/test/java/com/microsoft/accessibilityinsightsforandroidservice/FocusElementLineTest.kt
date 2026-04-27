// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FocusElementLineTest {
    lateinit var testSubject: FocusElementLine

    @Mock
    lateinit var eventSourceMock: AccessibilityNodeInfo

    @Mock
    lateinit var previousEventSourceMock: AccessibilityNodeInfo

    @Mock
    lateinit var foregroundLinePaintMock: Paint

    @Mock
    lateinit var backgroundLinePaintMock: Paint

    @Mock
    lateinit var differentBackgroundLinePaintMock: Paint

    @Mock
    lateinit var viewMock: View

    @Mock
    lateinit var resourcesMock: Resources

    @Mock
    lateinit var canvasMock: Canvas
    lateinit  var rectConstructionMock: MockedConstruction<Rect>

    lateinit var initialPaints: HashMap<String, Paint>

    @Before
    @Throws(Exception::class)
    fun prepare() {
        initialPaints = HashMap()
        initialPaints["foregroundLine"] = foregroundLinePaintMock
        initialPaints["backgroundLine"] = backgroundLinePaintMock

        Mockito.`when`(viewMock.resources).thenReturn(resourcesMock)
        rectConstructionMock = Mockito.mockConstruction(Rect::class.java)

        testSubject =
            FocusElementLine(eventSourceMock, previousEventSourceMock, initialPaints, viewMock)
    }

    @After
    fun cleanUp() {
        rectConstructionMock.close()
    }

    @Test
    fun returnsNotNull() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    @Throws(Exception::class)
    fun drawLineDrawsOneForegroundAndOneBackgroundLine() {
        Mockito.`when`(eventSourceMock.refresh()).thenReturn(true)
        Mockito.`when`(previousEventSourceMock.refresh()).thenReturn(true)

        testSubject.drawLine(canvasMock)
        Mockito.verify(canvasMock, Mockito.times(1))
            .drawLine(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(foregroundLinePaintMock)
            )
        Mockito.verify(canvasMock, Mockito.times(1))
            .drawLine(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(backgroundLinePaintMock)
            )
        Mockito.verifyNoMoreInteractions(canvasMock)
    }

    @Test
    @Throws(Exception::class)
    fun setPaintUpdatesPaintsUsedToDrawLines() {
        Mockito.`when`(eventSourceMock.refresh()).thenReturn(true)
        Mockito.`when`(previousEventSourceMock.refresh()).thenReturn(true)

        val updatedPaints = HashMap<String, Paint>(initialPaints)
        updatedPaints["backgroundLine"] = differentBackgroundLinePaintMock

        testSubject.setPaint(updatedPaints)
        testSubject.drawLine(canvasMock)

        Mockito.verify(canvasMock, Mockito.times(0))
            .drawLine(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same( /* original */backgroundLinePaintMock)
            )
        Mockito.verify(canvasMock, Mockito.times(1))
            .drawLine(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(differentBackgroundLinePaintMock)
            )
    }

    @Test
    fun drawLineDoesNothingWhenEventSourceIsNull() {
        testSubject = FocusElementLine(null, previousEventSourceMock, initialPaints, viewMock)
        testSubject.drawLine(canvasMock)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    fun drawLineDoesNothingWhenPreviousEventSourceIsNull() {
        testSubject = FocusElementLine(eventSourceMock, null, initialPaints, viewMock)
        testSubject.drawLine(canvasMock)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    fun drawLineDoesNothingWhenPreviousEventSourceDoesNotRefresh() {
        Mockito.`when`(eventSourceMock.refresh()).thenReturn(true)
        Mockito.`when`(previousEventSourceMock.refresh()).thenReturn(false)
        testSubject.drawLine(canvasMock)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    fun drawLineDoesNothingWhenEventSourceDoesNotRefresh() {
        Mockito.`when`(eventSourceMock.refresh()).thenReturn(false)
        testSubject.drawLine(canvasMock)
        Mockito.verifyNoInteractions(canvasMock)
    }
}
