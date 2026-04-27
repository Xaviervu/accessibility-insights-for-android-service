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
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FocusElementHighlightTest {
    var testSubject: FocusElementHighlight? = null

    @Mock
    var accessibilityNodeInfoMock: AccessibilityNodeInfo? = null

    @Mock
    var viewMock: View? = null

    @Mock
    var innerCirclePaintMock: Paint? = null

    @Mock
    var outerCirclePaintMock: Paint? = null

    @Mock
    var differentOuterCirclePaintMock: Paint? = null

    @Mock
    var numberPaintMock: Paint? = null

    @Mock
    var transparentInnerCirclePaintMock: Paint? = null

    @Mock
    var resourcesMock: Resources? = null

    @Mock
    var canvasMock: Canvas? = null
    var rectConstructionMock: MockedConstruction<Rect?>? = null

    var initialPaints: HashMap<String?, Paint?>? = null

    var tabStopCount: Int = 10

    @Before
    @Throws(Exception::class)
    fun prepare() {
        initialPaints = HashMap<String?, Paint?>()
        initialPaints!!.put("innerCircle", innerCirclePaintMock)
        initialPaints!!.put("outerCircle", outerCirclePaintMock)
        initialPaints!!.put("number", numberPaintMock)
        initialPaints!!.put("transparentInnerCircle", transparentInnerCirclePaintMock)

        Mockito.`when`<Resources?>(viewMock!!.getResources()).thenReturn(resourcesMock)
        rectConstructionMock = Mockito.mockConstruction<Rect?>(Rect::class.java)

        testSubject =
            FocusElementHighlight(
                accessibilityNodeInfoMock!!, initialPaints, 10, tabStopCount, viewMock!!
            )
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
    fun drawElementHighlightDoesNothingWhenEventSourceIsNull() {
        testSubject = FocusElementHighlight(null, initialPaints, 10, 10, viewMock!!)
        testSubject!!.drawElementHighlight(canvasMock!!)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    fun drawElementHighlightDoesNothingWhenEventSourceRefreshDoesNotWork() {
        Mockito.`when`<Boolean?>(accessibilityNodeInfoMock!!.refresh()).thenReturn(false)
        testSubject!!.drawElementHighlight(canvasMock!!)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    @Throws(Exception::class)
    fun drawElementHighlightDrawsTwoCirclesForCurrentElement() {
        Mockito.`when`<Boolean?>(accessibilityNodeInfoMock!!.refresh()).thenReturn(true)

        testSubject!!.drawElementHighlight(canvasMock!!)

        Mockito.verify<Canvas?>(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(transparentInnerCirclePaintMock)
            )
        Mockito.verify<Canvas?>(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(outerCirclePaintMock)
            )
        Mockito.verifyNoMoreInteractions(canvasMock)
    }

    @Test
    @Throws(Exception::class)
    fun drawElementHighlightDrawsTwoCirclesAndANumberForNonCurrentElement() {
        testSubject!!.setAsNonCurrentElement()
        Mockito.`when`<Boolean?>(accessibilityNodeInfoMock!!.refresh()).thenReturn(true)
        testSubject!!.drawElementHighlight(canvasMock!!)

        Mockito.verify<Canvas?>(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(innerCirclePaintMock)
            )
        Mockito.verify<Canvas?>(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(outerCirclePaintMock)
            )
        val expectedText = tabStopCount.toString() + ""
        Mockito.verify<Canvas?>(canvasMock, VerificationModeFactory.times(1))
            .drawText(
                ArgumentMatchers.eq<String?>(expectedText),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(numberPaintMock)
            )
        Mockito.verifyNoMoreInteractions(canvasMock)
    }

    @Test
    @Throws(Exception::class)
    fun setPaintsModifiesPaintsUsedToDrawElementHighlights() {
        Mockito.`when`<Boolean?>(accessibilityNodeInfoMock!!.refresh()).thenReturn(true)

        val updatedPaints = HashMap<String?, Paint?>(initialPaints)
        updatedPaints.put("outerCircle", differentOuterCirclePaintMock)

        testSubject!!.setPaints(updatedPaints)
        testSubject!!.drawElementHighlight(canvasMock!!)

        Mockito.verify<Canvas?>(canvasMock, VerificationModeFactory.times(0))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),  /* original */
                ArgumentMatchers.same<Paint?>(outerCirclePaintMock)
            )
        Mockito.verify<Canvas?>(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same<Paint?>(differentOuterCirclePaintMock)
            )
    }

    @Test
    fun getEventSourceReturnsAccessibilityNodeInfo() {
        Assert.assertEquals(testSubject!!.eventSource, accessibilityNodeInfoMock)
    }
}
