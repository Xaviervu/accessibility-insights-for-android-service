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
    var testSubject: FocusElementLine? = null

    @Mock
    var eventSourceMock: AccessibilityNodeInfo? = null

    @Mock
    var previousEventSourceMock: AccessibilityNodeInfo? = null

    @Mock
    var foregroundLinePaintMock: Paint? = null

    @Mock
    var backgroundLinePaintMock: Paint? = null

    @Mock
    var differentBackgroundLinePaintMock: Paint? = null

    @Mock
    var viewMock: View? = null

    @Mock
    var resourcesMock: Resources? = null

    @Mock
    var canvasMock: Canvas? = null
    var rectConstructionMock: MockedConstruction<Rect?>? = null

    var initialPaints: HashMap<String?, Paint?>? = null

    @Before
    @Throws(Exception::class)
    fun prepare() {
        initialPaints = HashMap<String?, Paint?>()
        initialPaints!!.put("foregroundLine", foregroundLinePaintMock)
        initialPaints!!.put("backgroundLine", backgroundLinePaintMock)

        Mockito.`when`<Resources?>(viewMock!!.getResources()).thenReturn(resourcesMock)
        rectConstructionMock = Mockito.mockConstruction<Rect?>(Rect::class.java)

        testSubject =
            FocusElementLine(eventSourceMock, previousEventSourceMock, initialPaints, viewMock!!)
    }

    @After
    fun cleanUp() {
        rectConstructionMock!!.close()
    }

    @Test
    fun returnsNotNull() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    @Throws(Exception::class)
    fun drawLineDrawsOneForegroundAndOneBackgroundLine() {
        Mockito.`when`<Boolean?>(eventSourceMock!!.refresh()).thenReturn(true)
        Mockito.`when`<Boolean?>(previousEventSourceMock!!.refresh()).thenReturn(true)

        testSubject!!.drawLine(canvasMock!!)
        Mockito.verify<Canvas?>(canvasMock, Mockito.times(1))
            .drawLine(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(foregroundLinePaintMock)
            )
        Mockito.verify<Canvas?>(canvasMock, Mockito.times(1))
            .drawLine(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(backgroundLinePaintMock)
            )
        Mockito.verifyNoMoreInteractions(canvasMock)
    }

    @Test
    @Throws(Exception::class)
    fun setPaintUpdatesPaintsUsedToDrawLines() {
        Mockito.`when`<Boolean?>(eventSourceMock!!.refresh()).thenReturn(true)
        Mockito.`when`<Boolean?>(previousEventSourceMock!!.refresh()).thenReturn(true)

        val updatedPaints = HashMap<String?, Paint?>(initialPaints)
        updatedPaints.put("backgroundLine", differentBackgroundLinePaintMock)

        testSubject!!.setPaint(updatedPaints)
        testSubject!!.drawLine(canvasMock!!)

        Mockito.verify<Canvas?>(canvasMock, Mockito.times(0))
            .drawLine(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>( /* original */backgroundLinePaintMock)
            )
        Mockito.verify<Canvas?>(canvasMock, Mockito.times(1))
            .drawLine(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(differentBackgroundLinePaintMock)
            )
    }

    @Test
    fun drawLineDoesNothingWhenEventSourceIsNull() {
        testSubject = FocusElementLine(null, previousEventSourceMock, initialPaints, viewMock!!)
        testSubject!!.drawLine(canvasMock!!)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    fun drawLineDoesNothingWhenPreviousEventSourceIsNull() {
        testSubject = FocusElementLine(eventSourceMock, null, initialPaints, viewMock!!)
        testSubject!!.drawLine(canvasMock!!)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    fun drawLineDoesNothingWhenPreviousEventSourceDoesNotRefresh() {
        Mockito.`when`<Boolean?>(eventSourceMock!!.refresh()).thenReturn(true)
        Mockito.`when`<Boolean?>(previousEventSourceMock!!.refresh()).thenReturn(false)
        testSubject!!.drawLine(canvasMock!!)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    fun drawLineDoesNothingWhenEventSourceDoesNotRefresh() {
        Mockito.`when`<Boolean?>(eventSourceMock!!.refresh()).thenReturn(false)
        testSubject!!.drawLine(canvasMock!!)
        Mockito.verifyNoInteractions(canvasMock)
    }
}
