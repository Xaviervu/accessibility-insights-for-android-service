// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Surface
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.function.Consumer
import java.util.function.Supplier

@RunWith(MockitoJUnitRunner::class)
class ScreenshotControllerTest {
    @Mock
    var displayMetricsSupplierMock: Supplier<DisplayMetrics?>? = null

    @Mock
    var handlerMock: Handler? = null

    @Mock
    var onScreenshotAvailableProviderMock: OnScreenshotAvailableProvider? = null

    @Mock
    var bitmapProviderMock: BitmapProvider? = null

    @Mock
    var bitmapConsumerMock: Consumer<Bitmap?>? = null

    @Mock
    var mediaProjectionSupplierMock: Supplier<MediaProjection?>? = null

    @Mock
    var mediaProjectionMock: MediaProjection? = null

    @Mock
    var imageReaderMock: ImageReader? = null

    @Mock
    var surfaceMock: Surface? = null

    @Mock
    var bitmapMock: Bitmap? = null

    @Mock
    var onScreenshotAvailableMock: OnScreenshotAvailable? = null

    @Mock
    var displayMock: VirtualDisplay? = null

    var imageReaderStaticMock: MockedStatic<ImageReader?>? = null

    @Captor
    var bitmapConsumerCallback: ArgumentCaptor<Consumer<Bitmap?>?>? = null

    var displayMetricsStub: DisplayMetrics? = null
    var testSubject: ScreenshotController? = null

    @Before
    fun prepare() {
        imageReaderStaticMock = Mockito.mockStatic<ImageReader?>(ImageReader::class.java)
        displayMetricsStub = DisplayMetricsStub()
        testSubject =
            ScreenshotController(
                displayMetricsSupplierMock,
                handlerMock,
                onScreenshotAvailableProviderMock!!,
                bitmapProviderMock!!,
                mediaProjectionSupplierMock!!
            )
    }

    @After
    fun cleanUp() {
        imageReaderStaticMock!!.close()
    }

    @Test
    fun screenshotControllerIsNotNull() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun nullBitmapReturnedWhenSharedMediaProjectionIsNull() {
        Mockito.`when`<MediaProjection?>(mediaProjectionSupplierMock!!.get()).thenReturn(null)

        testSubject!!.getScreenshotWithMediaProjection(bitmapConsumerMock!!)

        Mockito.verify<Consumer<Bitmap?>?>(bitmapConsumerMock, Mockito.times(1)).accept(null)
    }

    @Test
    fun createVirtualDisplayWithExpectedImageReader() {
        bitmapConsumerCallback = createBitmapConsumerCallback()
        Mockito.`when`<MediaProjection?>(mediaProjectionSupplierMock!!.get())
            .thenReturn(mediaProjectionMock)
        Mockito.`when`<DisplayMetrics?>(displayMetricsSupplierMock!!.get())
            .thenReturn(displayMetricsStub)
        imageReaderStaticMock!!
            .`when`<Any?>(
                Verification {
                    ImageReader.newInstance(
                        displayMetricsStub!!.widthPixels,
                        displayMetricsStub!!.heightPixels,
                        PixelFormat.RGBA_8888,
                        2
                    )
                })
            .thenReturn(imageReaderMock)
        Mockito.`when`<Surface?>(imageReaderMock!!.getSurface()).thenReturn(surfaceMock)
        Mockito.`when`<OnScreenshotAvailable>(
            onScreenshotAvailableProviderMock!!.getOnScreenshotAvailable(
                ArgumentMatchers.eq<DisplayMetrics?>(displayMetricsStub),
                ArgumentMatchers.eq<BitmapProvider?>(bitmapProviderMock),
                bitmapConsumerCallback!!.capture()
            )
        )
            .thenReturn(onScreenshotAvailableMock)
        Mockito.`when`<VirtualDisplay?>(
            mediaProjectionMock!!.createVirtualDisplay(
                "myDisplay",
                displayMetricsStub!!.widthPixels,
                displayMetricsStub!!.heightPixels,
                displayMetricsStub!!.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surfaceMock,
                null,
                null
            )
        )
            .thenReturn(displayMock)

        testSubject!!.getScreenshotWithMediaProjection(bitmapConsumerMock!!)
        bitmapConsumerCallback!!.getValue()!!.accept(bitmapMock)

        Mockito.verify<VirtualDisplay?>(displayMock, Mockito.times(1)).release()
        Mockito.verify<Consumer<Bitmap?>?>(bitmapConsumerMock, Mockito.times(1)).accept(bitmapMock)
    }

    private fun createBitmapConsumerCallback(): ArgumentCaptor<Consumer<Bitmap?>?> {
        return ArgumentCaptor.forClass<Consumer<Bitmap?>?, Consumer<*>?>(Consumer::class.java)
    }

    @Test
    fun createVirtualDisplayCleansResourcesAppropriatelyBeforeGettingScreenshot() {
        Mockito.`when`<MediaProjection?>(mediaProjectionSupplierMock!!.get())
            .thenReturn(mediaProjectionMock)
        Mockito.`when`<DisplayMetrics?>(displayMetricsSupplierMock!!.get())
            .thenReturn(displayMetricsStub)
        imageReaderStaticMock!!
            .`when`<Any?>(
                Verification {
                    ImageReader.newInstance(
                        displayMetricsStub!!.widthPixels,
                        displayMetricsStub!!.heightPixels,
                        PixelFormat.RGBA_8888,
                        2
                    )
                })
            .thenReturn(imageReaderMock)
        Mockito.`when`<Surface?>(imageReaderMock!!.getSurface()).thenReturn(surfaceMock)
        Mockito.`when`<VirtualDisplay?>(
            mediaProjectionMock!!.createVirtualDisplay(
                "myDisplay",
                displayMetricsStub!!.widthPixels,
                displayMetricsStub!!.heightPixels,
                displayMetricsStub!!.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surfaceMock,
                null,
                null
            )
        )
            .thenReturn(displayMock)

        testSubject!!.getScreenshotWithMediaProjection(bitmapConsumerMock!!)
        testSubject!!.getScreenshotWithMediaProjection(bitmapConsumerMock!!)

        Mockito.verify<VirtualDisplay?>(displayMock, Mockito.times(1)).release()
        Mockito.verify<ImageReader?>(imageReaderMock, Mockito.times(1)).close()
    }
}
