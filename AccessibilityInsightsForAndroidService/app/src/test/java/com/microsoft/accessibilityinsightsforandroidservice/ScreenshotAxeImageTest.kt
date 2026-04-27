// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.util.Base64
import com.deque.axe.android.colorcontrast.AxeColor
import com.deque.axe.android.wrappers.AxeRect
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.ByteArrayOutputStream

@RunWith(MockitoJUnitRunner::class)
class ScreenshotAxeImageTest {
    @Mock
    var bitmapMock: Bitmap? = null

    @Mock
    var byteArrayOutputStreamProviderMock: ByteArrayOutputStreamProvider? = null

    @Mock
    var byteArrayOutputStreamMock: ByteArrayOutputStream? = null

    var sampleWidth: Int = 0
    var sampleHeight: Int = 0
    var testSubject: ScreenshotAxeImage? = null

    @Before
    fun prepare() {
        sampleHeight = 100
        sampleWidth = 50

        Mockito.`when`<Int?>(bitmapMock!!.getWidth()).thenReturn(sampleWidth)
        Mockito.`when`<Int?>(bitmapMock!!.getHeight()).thenReturn(sampleHeight)

        testSubject = ScreenshotAxeImage(bitmapMock!!, byteArrayOutputStreamProviderMock!!)
    }

    @Test
    fun screenShotAxeImageExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun pixelReturnsCorrectColor() {
        val givenX = 10
        val givenY = 20
        val colorIntStub = 100

        Mockito.`when`<Int?>(bitmapMock!!.getPixel(givenX, givenY)).thenReturn(colorIntStub)

        val returnedAxeColor = testSubject!!.pixel(givenX, givenY)

        Assert.assertEquals(returnedAxeColor, AxeColor(colorIntStub))
    }

    @Test
    fun frameReturnsCorrectRect() {
        val expectedRect = AxeRect(0, sampleWidth - 1, 0, sampleHeight - 1)

        Assert.assertEquals(testSubject!!.frame(), expectedRect)
    }

    @Test
    fun toBase64PngReturnsCorrectString() {
        val byteArrayStub = ByteArray(1)
        val expectedString = "some string"

        Mockito.mockStatic<Base64?>(Base64::class.java).use { base64StaticMock ->
            Mockito
                .`when`<ByteArrayOutputStream>(byteArrayOutputStreamProviderMock!!.get())
                .thenReturn(byteArrayOutputStreamMock)
            Mockito
                .`when`<ByteArray?>(byteArrayOutputStreamMock!!.toByteArray())
                .thenReturn(byteArrayStub)
            base64StaticMock
                .`when`<Any?>(Verification { Base64.encodeToString(byteArrayStub, Base64.NO_WRAP) })
                .thenReturn(expectedString)

            Assert.assertEquals(testSubject!!.toBase64Png(), expectedString)
            Mockito
                .verify<Bitmap?>(bitmapMock, Mockito.times(1))
                .compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStreamMock!!)
        }
    }
}
