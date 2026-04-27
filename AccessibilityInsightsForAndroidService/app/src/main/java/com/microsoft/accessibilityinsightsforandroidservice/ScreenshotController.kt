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
import java.util.function.Consumer
import java.util.function.Supplier

class ScreenshotController(
    private val displayMetricsSupplier: Supplier<DisplayMetrics>,
    private val screenshotHandler: Handler?,
    private val onScreenshotAvailableProvider: OnScreenshotAvailableProvider,
    private val bitmapProvider: BitmapProvider,
    private val mediaProjectionSupplier: Supplier<MediaProjection?>?,
) {
    private lateinit var metrics: DisplayMetrics
    private var imageReader: ImageReader? = null
    private var display: VirtualDisplay? = null

    fun getScreenshotWithMediaProjection(bitmapConsumer: Consumer<Bitmap?>) {
        val sharedMediaProjection = mediaProjectionSupplier?.get()
        if(mediaProjectionSupplier == null) {
            bitmapConsumer.accept(null)
            return
        }
        imageReader?.close()

        display?.release()

        metrics = displayMetricsSupplier.get()
        imageReader = getImageReader(metrics, bitmapConsumer)
        display =
            sharedMediaProjection?.createVirtualDisplay(
                "myDisplay",
                metrics.widthPixels,
                metrics.heightPixels,
                metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null,
            )
    }

    private fun getImageReader(
        metrics: DisplayMetrics,
        bitmapConsumer: Consumer<Bitmap?>,
    ): ImageReader {
        val imageReader =
            ImageReader.newInstance(
                metrics.widthPixels,
                metrics.heightPixels, // The linter gives a false positive here because it wants us to use one of the
                // ImageFormat.* constants, but ImageReader.newInstance documents the PixelFormat
                // constants as being acceptable, too. We don't control the choice of format; it's
                // determined by the input data we get from the system screenshot functionality.
                //
                // noinspection WrongConstant
                PixelFormat.RGBA_8888,
                2,
            )

        val onBitmapAvailable =
            Consumer { bitmap: Bitmap ->
                display?.release()
                bitmapConsumer.accept(bitmap)
            }

        val onScreenshotAvailable =
            onScreenshotAvailableProvider.getOnScreenshotAvailable(
                metrics,
                bitmapProvider,
                onBitmapAvailable,
            )
        imageReader.setOnImageAvailableListener(onScreenshotAvailable, screenshotHandler)

        return imageReader
    }
}
