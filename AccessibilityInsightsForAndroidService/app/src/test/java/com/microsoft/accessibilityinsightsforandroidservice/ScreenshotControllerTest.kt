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
    lateinit var displayMetricsSupplierMock: Supplier<DisplayMetrics>

    @Mock
    lateinit var handlerMock: Handler

    @Mock
    lateinit var onScreenshotAvailableProviderMock: OnScreenshotAvailableProvider

    @Mock
    lateinit var bitmapProviderMock: BitmapProvider

    @Mock
    lateinit var bitmapConsumerMock: Consumer<Bitmap?>

    @Mock
    lateinit var mediaProjectionSupplierMock: Supplier<MediaProjection?>

    @Mock
    lateinit var mediaProjectionMock: MediaProjection

    @Mock
    lateinit var imageReaderMock: ImageReader

    @Mock
    lateinit var surfaceMock: Surface

    @Mock
    lateinit var bitmapMock: Bitmap

    @Mock
    lateinit var onScreenshotAvailableMock: OnScreenshotAvailable

    @Mock
    lateinit var displayMock: VirtualDisplay

    lateinit var imageReaderStaticMock: MockedStatic<ImageReader>

    @Captor
    lateinit var bitmapConsumerCallback: ArgumentCaptor<Consumer<Bitmap>>

    lateinit var displayMetricsStub: DisplayMetrics
    lateinit var testSubject: ScreenshotController

    @Before
    fun prepare() {
        imageReaderStaticMock = Mockito.mockStatic(ImageReader::class.java)
        displayMetricsStub = DisplayMetricsStub()
        testSubject =
            ScreenshotController(
                displayMetricsSupplierMock,
                handlerMock,
                onScreenshotAvailableProviderMock,
                bitmapProviderMock,
                mediaProjectionSupplierMock,
            )
    }

    @After
    fun cleanUp() {
        imageReaderStaticMock.close()
    }

    @Test
    fun screenshotControllerIsNotNull() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun nullBitmapReturnedWhenSharedMediaProjectionIsNull() {
        Mockito.`when`(mediaProjectionSupplierMock.get()).thenReturn(null)

        testSubject.getScreenshotWithMediaProjection(bitmapConsumerMock)

        Mockito.verify(bitmapConsumerMock, Mockito.times(1)).accept(null)
    }

    @Test
    fun createVirtualDisplayWithExpectedImageReader() {
        bitmapConsumerCallback = createBitmapConsumerCallback()
        Mockito
            .`when`(mediaProjectionSupplierMock.get())
            .thenReturn(mediaProjectionMock)
        Mockito
            .`when`(displayMetricsSupplierMock.get())
            .thenReturn(displayMetricsStub)
        imageReaderStaticMock
            .`when`<Any?>(
                Verification {
                    ImageReader.newInstance(
                        displayMetricsStub.widthPixels,
                        displayMetricsStub.heightPixels,
                        PixelFormat.RGBA_8888,
                        2,
                    )
                },
            ).thenReturn(imageReaderMock)
        Mockito.`when`<Surface?>(imageReaderMock.surface).thenReturn(surfaceMock)
        Mockito
            .`when`<OnScreenshotAvailable>(
                onScreenshotAvailableProviderMock.getOnScreenshotAvailable(
                    ArgumentMatchers.eq(displayMetricsStub),
                    ArgumentMatchers.eq(bitmapProviderMock),
                    bitmapConsumerCallback.capture(),
                ),
            ).thenReturn(onScreenshotAvailableMock)
        Mockito
            .`when`<VirtualDisplay?>(
                mediaProjectionMock.createVirtualDisplay(
                    "myDisplay",
                    displayMetricsStub.widthPixels,
                    displayMetricsStub.heightPixels,
                    displayMetricsStub.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surfaceMock,
                    null,
                    null,
                ),
            ).thenReturn(displayMock)

        testSubject.getScreenshotWithMediaProjection(bitmapConsumerMock)
        bitmapConsumerCallback.getValue().accept(bitmapMock)

        Mockito.verify(displayMock, Mockito.times(1)).release()
        Mockito.verify(bitmapConsumerMock, Mockito.times(1)).accept(bitmapMock)
    }

   private fun createBitmapConsumerCallback(): ArgumentCaptor<Consumer<Bitmap>> =
        ArgumentCaptor.forClass(Consumer::class.java) as ArgumentCaptor<Consumer<Bitmap>>

    @Test
    fun createVirtualDisplayCleansResourcesAppropriatelyBeforeGettingScreenshot() {
        Mockito
            .`when`(mediaProjectionSupplierMock.get())
            .thenReturn(mediaProjectionMock)
        Mockito
            .`when`(displayMetricsSupplierMock.get())
            .thenReturn(displayMetricsStub)
        imageReaderStaticMock
            .`when`<Any?> {
                ImageReader.newInstance(
                    displayMetricsStub.widthPixels,
                    displayMetricsStub.heightPixels,
                    PixelFormat.RGBA_8888,
                    2,
                )
            }.thenReturn(imageReaderMock)
        Mockito.`when`<Surface?>(imageReaderMock.surface).thenReturn(surfaceMock)
        Mockito
            .`when`<VirtualDisplay?>(
                mediaProjectionMock.createVirtualDisplay(
                    "myDisplay",
                    displayMetricsStub.widthPixels,
                    displayMetricsStub.heightPixels,
                    displayMetricsStub.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surfaceMock,
                    null,
                    null,
                ),
            ).thenReturn(displayMock)

        testSubject.getScreenshotWithMediaProjection(bitmapConsumerMock)
        testSubject.getScreenshotWithMediaProjection(bitmapConsumerMock)

        Mockito.verify(displayMock, Mockito.times(1)).release()
        Mockito.verify(imageReaderMock, Mockito.times(1)).close()
    }
}
