// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeDevice
import com.deque.axe.android.AxeView
import com.deque.axe.android.colorcontrast.AxeImage
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeContextFactory
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeDeviceFactory
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeImageFactory
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeViewsFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AxeContextFactoryTest {
    @Mock
    var axeImageFactoryMock: AxeImageFactory? = null

    @Mock
    var axeImageMock: AxeImage? = null

    @Mock
    var axeViewsFactoryMock: AxeViewsFactory? = null

    @Mock
    var axeViewMock: AxeView? = null

    @Mock
    var axeDeviceFactoryMock: AxeDeviceFactory? = null

    @Mock
    var axeDeviceMock: AxeDevice? = null

    @Mock
    var rootNodeMock: AccessibilityNodeInfo? = null

    @Mock
    var screenshotMock: Bitmap? = null

    var testSubject: AxeContextFactory? = null

    @Before
    @Throws(ViewChangedException::class)
    fun prepare() {
        Mockito.`when`<AxeImage?>(axeImageFactoryMock!!.createAxeImage(screenshotMock))
            .thenReturn(axeImageMock)
        Mockito.`when`<AxeView?>(axeViewsFactoryMock!!.createAxeViews(rootNodeMock!!))
            .thenReturn(axeViewMock)
        Mockito.`when`<AxeDevice>(axeDeviceFactoryMock!!.createAxeDevice(rootNodeMock))
            .thenReturn(axeDeviceMock)

        testSubject =
            AxeContextFactory(axeImageFactoryMock!!, axeViewsFactoryMock!!, axeDeviceFactoryMock!!)
    }

    @Test
    @Throws(ViewChangedException::class)
    fun axeContentIsNotNull() {
        Assert.assertNotNull(testSubject!!.createAxeContext(rootNodeMock!!, screenshotMock!!))
    }

    @Test
    @Throws(ViewChangedException::class)
    fun axeContentHasCorrectProperties() {
        val axeContext = testSubject!!.createAxeContext(rootNodeMock!!, screenshotMock!!)
        Assert.assertEquals(axeContext.screenshot, axeImageMock)
        Assert.assertEquals(axeContext.axeDevice, axeDeviceMock)
        Assert.assertEquals(axeContext.axeView, axeViewMock)
        Assert.assertNotNull(axeContext.axeEventStream)
    }
}
