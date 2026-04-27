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
    lateinit var testSubject: FocusElementHighlight

    @Mock
    lateinit var accessibilityNodeInfoMock: AccessibilityNodeInfo

    @Mock
    lateinit var viewMock: View

    @Mock
    lateinit var innerCirclePaintMock: Paint

    @Mock
    lateinit var outerCirclePaintMock: Paint

    @Mock
    lateinit var differentOuterCirclePaintMock: Paint

    @Mock
    lateinit var numberPaintMock: Paint

    @Mock
    lateinit var transparentInnerCirclePaintMock: Paint

    @Mock
    lateinit var resourcesMock: Resources

    @Mock
    lateinit var canvasMock: Canvas
    lateinit var rectConstructionMock: MockedConstruction<Rect>

    lateinit var initialPaints: HashMap<String, Paint>

    var tabStopCount: Int = 10

    @Before
    @Throws(Exception::class)
    fun prepare() {
        initialPaints = HashMap()
        initialPaints["innerCircle"] = innerCirclePaintMock
        initialPaints["outerCircle"] = outerCirclePaintMock
        initialPaints["number"] = numberPaintMock
        initialPaints["transparentInnerCircle"] = transparentInnerCirclePaintMock

        Mockito.`when`<Resources?>(viewMock.resources).thenReturn(resourcesMock)
        rectConstructionMock = Mockito.mockConstruction(Rect::class.java)

        testSubject =
            FocusElementHighlight(
                accessibilityNodeInfoMock,
                initialPaints,
                10,
                tabStopCount,
                viewMock,
            )
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
    fun drawElementHighlightDoesNothingWhenEventSourceIsNull() {
        testSubject = FocusElementHighlight(null, initialPaints, 10, 10, viewMock!!)
        testSubject.drawElementHighlight(canvasMock)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    fun drawElementHighlightDoesNothingWhenEventSourceRefreshDoesNotWork() {
        Mockito.`when`(accessibilityNodeInfoMock.refresh()).thenReturn(false)
        testSubject.drawElementHighlight(canvasMock)
        Mockito.verifyNoInteractions(canvasMock)
    }

    @Test
    @Throws(Exception::class)
    fun drawElementHighlightDrawsTwoCirclesForCurrentElement() {
        Mockito.`when`(accessibilityNodeInfoMock.refresh()).thenReturn(true)

        testSubject.drawElementHighlight(canvasMock)

        Mockito
            .verify(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(transparentInnerCirclePaintMock),
            )
        Mockito
            .verify(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(outerCirclePaintMock),
            )
        Mockito.verifyNoMoreInteractions(canvasMock)
    }

    @Test
    @Throws(Exception::class)
    fun drawElementHighlightDrawsTwoCirclesAndANumberForNonCurrentElement() {
        testSubject.setAsNonCurrentElement()
        Mockito.`when`(accessibilityNodeInfoMock.refresh()).thenReturn(true)
        testSubject.drawElementHighlight(canvasMock)

        Mockito
            .verify(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(innerCirclePaintMock),
            )
        Mockito
            .verify(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(outerCirclePaintMock),
            )
        val expectedText = tabStopCount.toString() + ""
        Mockito
            .verify(canvasMock, VerificationModeFactory.times(1))
            .drawText(
                ArgumentMatchers.eq(expectedText),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(numberPaintMock),
            )
        Mockito.verifyNoMoreInteractions(canvasMock)
    }

    @Test
    @Throws(Exception::class)
    fun setPaintsModifiesPaintsUsedToDrawElementHighlights() {
        Mockito.`when`(accessibilityNodeInfoMock.refresh()).thenReturn(true)

        val updatedPaints = HashMap<String, Paint>(initialPaints)
        updatedPaints["outerCircle"] = differentOuterCirclePaintMock

        testSubject.setPaints(updatedPaints)
        testSubject.drawElementHighlight(canvasMock)

        Mockito
            .verify(canvasMock, VerificationModeFactory.times(0))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(), // original
                ArgumentMatchers.same(outerCirclePaintMock),
            )
        Mockito
            .verify(canvasMock, VerificationModeFactory.times(1))
            .drawCircle(
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.anyFloat(),
                ArgumentMatchers.same(differentOuterCirclePaintMock),
            )
    }

    @Test
    fun getEventSourceReturnsAccessibilityNodeInfo() {
        Assert.assertEquals(testSubject.eventSource, accessibilityNodeInfoMock)
    }
}
