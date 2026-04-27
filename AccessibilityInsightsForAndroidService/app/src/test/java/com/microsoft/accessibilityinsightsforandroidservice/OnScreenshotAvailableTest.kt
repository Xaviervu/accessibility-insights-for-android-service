// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.media.Image
import android.media.Image.Plane
import android.media.ImageReader
import android.util.DisplayMetrics
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.nio.Buffer
import java.nio.ByteBuffer
import java.util.function.Consumer

@RunWith(MockitoJUnitRunner::class)
class OnScreenshotAvailableTest {
    @Mock
    lateinit var imageReaderMock: ImageReader

    @Mock
    lateinit var bitmapConsumerMock: Consumer<Bitmap>

    @Mock
    lateinit var imageMock: Image

    @Mock
    lateinit var imagePlaneMock: Plane

    @Mock
    lateinit var bitmapProviderMock: BitmapProvider

    @Mock
    lateinit var bitmapMock: Bitmap

    var loggerStaticMock: MockedStatic<Logger?>? = null

    lateinit var imagePlanesStub: Array<Plane>
    var testSubject: OnScreenshotAvailable? = null
    var widthStub: Int = 0
    var heightStub: Int = 0
    lateinit var imagePlaneStubBuffer: ByteBuffer
    var pixelStrideStub: Int = 0
    var rowStrideStub: Int = 0

    @Before
    fun prepare() {
        loggerStaticMock = Mockito.mockStatic<Logger?>(Logger::class.java)

        val metricsStub = DisplayMetrics()
        metricsStub.widthPixels = widthStub
        metricsStub.heightPixels = heightStub
        widthStub = 100
        heightStub = 200
        pixelStrideStub = 4
        rowStrideStub = widthStub * pixelStrideStub
        imagePlanesStub = arrayOf()
        imagePlanesStub[0] = imagePlaneMock

        testSubject = OnScreenshotAvailable(metricsStub, bitmapProviderMock, bitmapConsumerMock)
    }

    @After
    fun cleanup() {
        loggerStaticMock!!.close()
    }

    @Test
    fun onScreenshotAvailableIsNotNull() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun onImageAvailableIgnoresImagesWithInvalidPixelStrides() {
        // pixelStride should be the number of bytes per pixel; our input should always be in ARGB_8888
        // format, so it should be fixed at 4. But if it's not, we shouldn't crash.
        pixelStrideStub = 5

        setupMocksToCreateBitmap()

        testSubject!!.onImageAvailable(imageReaderMock)

        Mockito
            .verify<Bitmap>(bitmapMock, Mockito.times(0))
            .copyPixelsFromBuffer(imagePlaneStubBuffer)
        Mockito.verify(bitmapConsumerMock, Mockito.times(0)).accept(bitmapMock)
    }

    @Test
    fun onImageAvailableIgnoresImagesWithInvalidRowStrides() {
        // rowStride should be the number of bytes per row plus optionally some padding; it should
        // never be lower than widthStub * pixelStrideStub, but if it is, we shouldn't crash.
        rowStrideStub = widthStub * pixelStrideStub - 1

        setupMocksToCreateBitmap()

        testSubject!!.onImageAvailable(imageReaderMock)

        Mockito
            .verify(bitmapMock, Mockito.times(0))
            .copyPixelsFromBuffer(imagePlaneStubBuffer)
        Mockito.verify(bitmapConsumerMock, Mockito.times(0)).accept(bitmapMock)
    }

    @Test
    fun onImageAvailableWithUnpaddedImageBufferCreatesBitmapDirectlyFromSourceBuffer() {
        setupMocksToCreateBitmap()

        testSubject!!.onImageAvailable(imageReaderMock)

        Mockito
            .verify(bitmapMock, Mockito.times(1))
            .copyPixelsFromBuffer(imagePlaneStubBuffer)
        Mockito.verify(bitmapConsumerMock, Mockito.times(1)).accept(bitmapMock)
    }

    @Test
    fun onImageAvailableWithPaddedImageBufferStripsPaddingBeforeCopyingPixels() {
        widthStub = 2
        heightStub = 2
        val rowPaddingBytes = 1
        rowStrideStub = (pixelStrideStub * widthStub + rowPaddingBytes)

        imagePlaneStubBuffer =
            ByteBuffer.wrap(byteArrayOf(1, 1, 1, 1, 2, 2, 2, 2, 0, 3, 3, 3, 3, 4, 4, 4, 4, 0))
        val bufferWithPaddingRemoved =
            ByteBuffer.wrap(byteArrayOf(1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4))

        setupMocksToCreateBitmap()

        testSubject!!.onImageAvailable(imageReaderMock)

        Mockito
            .verify(bitmapMock, Mockito.times(1))
            .copyPixelsFromBuffer(ArgumentMatchers.eq(bufferWithPaddingRemoved))
        Mockito.verify(bitmapConsumerMock, Mockito.times(1)).accept(bitmapMock)
    }

    @Test
    fun onImageAvailableProcessesImageOnlyOnce() {
        setupMocksToCreateBitmap()
        testSubject!!.onImageAvailable(imageReaderMock)
        Mockito.reset<Any?>(
            imageReaderMock,
            bitmapConsumerMock,
            imageMock,
            imagePlaneMock,
            bitmapProviderMock,
            bitmapMock,
        )

        testSubject!!.onImageAvailable(imageReaderMock)

        Mockito
            .verify(bitmapMock, Mockito.times(0))
            .copyPixelsFromBuffer(ArgumentMatchers.any())
        Mockito
            .verify(bitmapConsumerMock, Mockito.times(0))
            .accept(ArgumentMatchers.any())
    }

    private fun setupMocksToCreateBitmap() {
        Mockito.`when`(imageReaderMock.acquireLatestImage()).thenReturn(imageMock)
        Mockito.`when`(imageMock.planes).thenReturn(imagePlanesStub)
        Mockito.`when`(imagePlaneMock.pixelStride).thenReturn(pixelStrideStub)
        Mockito.`when`(imagePlaneMock.rowStride).thenReturn(rowStrideStub)
        Mockito.`when`(imageMock.width).thenReturn(widthStub)
        Mockito.`when`(imageMock.height).thenReturn(heightStub)
        Mockito.`when`(imagePlaneMock.buffer).thenReturn(imagePlaneStubBuffer)
        Mockito
            .`when`<Bitmap>(
                bitmapProviderMock.createBitmap(
                    widthStub,
                    heightStub,
                    Bitmap.Config.ARGB_8888,
                ),
            ).thenReturn(bitmapMock)
    }
}
