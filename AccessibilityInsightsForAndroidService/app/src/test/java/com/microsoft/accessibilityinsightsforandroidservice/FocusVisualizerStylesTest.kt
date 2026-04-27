// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Color
import android.graphics.Paint
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FocusVisualizerStylesTest {
    var testSubject: FocusVisualizerStyles? = null

    var paintConstructionMock: MockedConstruction<Paint?>? = null
    var colorStaticMock: MockedStatic<Color?>? = null

    @Before
    @Throws(Exception::class)
    fun prepare() {
        paintConstructionMock = Mockito.mockConstruction<Paint?>(Paint::class.java)
        colorStaticMock = Mockito.mockStatic<Color?>(Color::class.java)
        testSubject = FocusVisualizerStyles()
    }

    @After
    fun cleanUp() {
        colorStaticMock!!.close()
        paintConstructionMock!!.close()
    }

    @Test
    fun getCurrentElementPaintsReturnsAllRelevantPaints() {
        val paints: HashMap<String?, Paint?> = testSubject!!.currentElementPaints
        Assert.assertNotNull(paints.get("outerCircle"))
        Assert.assertNotNull(paints.get("innerCircle"))
        Assert.assertNotNull(paints.get("number"))
        Assert.assertNotNull(paints.get("transparentInnerCircle"))
    }

    @Test
    fun getNonCurrentElementPaintsReturnsAllRelevantPaints() {
        val paints: HashMap<String?, Paint?> = testSubject!!.nonCurrentElementPaints
        Assert.assertNotNull(paints.get("outerCircle"))
        Assert.assertNotNull(paints.get("innerCircle"))
        Assert.assertNotNull(paints.get("number"))
    }

    @Test
    fun getNonCurrentLinePaintsReturnsAllRelevantPaints() {
        val paints: HashMap<String?, Paint?> = testSubject!!.nonCurrentLinePaints
        Assert.assertNotNull(paints.get("foregroundLine"))
        Assert.assertNotNull(paints.get("backgroundLine"))
    }

    @Test
    fun getCurrentLinePaintsReturnsAllRelevantPaints() {
        val paints: HashMap<String?, Paint?> = testSubject!!.currentLinePaints
        Assert.assertNotNull(paints.get("foregroundLine"))
        Assert.assertNotNull(paints.get("backgroundLine"))
    }
}
