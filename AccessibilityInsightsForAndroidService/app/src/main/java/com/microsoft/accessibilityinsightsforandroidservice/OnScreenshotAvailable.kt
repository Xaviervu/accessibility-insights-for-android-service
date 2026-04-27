// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.media.Image
import android.media.Image.Plane
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.util.DisplayMetrics
import com.microsoft.accessibilityinsightsforandroidservice.Logger.logError
import java.nio.ByteBuffer
import java.util.function.Consumer

class OnScreenshotAvailable(
    private val metrics: DisplayMetrics,
    private val bitmapProvider: BitmapProvider,
    private val bitmapConsumer: Consumer<Bitmap>
) : OnImageAvailableListener {
    private val IMAGE_BITMAP_FORMAT = Bitmap.Config.ARGB_8888
    private var imageAlreadyProcessed = false

    @Synchronized
    override fun onImageAvailable(imageReader: ImageReader) {
        // onImageAvailable can be called more than once; we only want one screenshot to be processed.
        if (imageAlreadyProcessed) {
            return
        }

        val image = imageReader.acquireLatestImage()
        var screenshotBitmap: Bitmap? = null
        try {
            screenshotBitmap = getBitmapFromImage(image)
        } catch (e: ImageFormatException) {
            logError(TAG, "ImageFormatException: $e")
        } finally {
            image.close()
        }

        // If we failed to convert the image, we just log an error and don't forward anything on to the
        // consumer that's forming the API response. From the API consumer's perspective, it will
        // propagate as results with no screenshot data available.
        if (screenshotBitmap != null) {
            imageAlreadyProcessed = true
            bitmapConsumer.accept(screenshotBitmap)
        }
    }

    @Throws(ImageFormatException::class)
    private fun getBitmapFromImage(image: Image): Bitmap {
        val width = image.getWidth()
        val height = image.getHeight()
        if (width != metrics.widthPixels || height != metrics.heightPixels) {
            logError(
                TAG,
                ("Received image of dimensions "
                        + width
                        + "x"
                        + height
                        + ", mismatches device DisplayMetrics "
                        + metrics.widthPixels
                        + "x"
                        + metrics.heightPixels)
            )
        }

        val bitmap = bitmapProvider.createBitmap(width, height, IMAGE_BITMAP_FORMAT)
        copyPixelsFromImagePlane(bitmap, image.getPlanes()[0], width, height)

        return bitmap
    }

    // The source Image.Plane and the destination Bitmap use the same byte encoding for image data,
    // 4 bytes per pixel in normal reading order, *except* that the Image.Plane can optionally contain
    // padding bytes at the end of each row's worth of pixel data, which the Bitmap doesn't support.
    //
    // The "row stride" refers to the number of bytes per row, *including* any optional padding.
    //
    // If the source doesn't use any padding, we copy its backing ByteBuffer directly into the
    // destination. If it *does* use padding, we create an intermediate ByteBuffer of our own and
    // selectively copy just the real/unpadded pixel data into it first.
    @Throws(ImageFormatException::class)
    private fun copyPixelsFromImagePlane(
        destination: Bitmap, source: Plane, width: Int, height: Int
    ) {
        val sourcePixelStride = source.getPixelStride() // bytes per pixel
        val sourceRowStride =
            source.getRowStride() // bytes per row, including any source row-padding
        val unpaddedRowStride = width * sourcePixelStride // bytes per row in destination

        if (sourcePixelStride != IMAGE_PIXEL_STRIDE) {
            throw ImageFormatException(
                ("Invalid source Image: sourcePixelStride="
                        + sourcePixelStride
                        + ", expected "
                        + IMAGE_PIXEL_STRIDE)
            )
        }
        if (sourceRowStride < unpaddedRowStride) {
            throw ImageFormatException(
                ("Invalid source Image: sourceRowStride "
                        + sourceRowStride
                        + " is too small for width "
                        + width
                        + " at sourcePixelStride "
                        + sourcePixelStride)
            )
        }

        val sourceBuffer = source.getBuffer()
        val bitmapPixelDataWithoutRowPadding: ByteBuffer

        if (sourceRowStride == unpaddedRowStride) {
            bitmapPixelDataWithoutRowPadding = sourceBuffer
        } else {
            bitmapPixelDataWithoutRowPadding = ByteBuffer.allocate(unpaddedRowStride * height)
            for (row in 0..<height) {
                val sourceOffset = row * sourceRowStride
                val destOffset = row * unpaddedRowStride
                sourceBuffer.position(sourceOffset)
                sourceBuffer.get(
                    bitmapPixelDataWithoutRowPadding.array(),
                    destOffset,
                    unpaddedRowStride
                )
            }
        }

        destination.copyPixelsFromBuffer(bitmapPixelDataWithoutRowPadding)
    }

    companion object {
        private const val IMAGE_PIXEL_STRIDE = 4 // Implied by ARGB_8888 (4 bytes per pixel)

        private const val TAG = "OnScreenshotAvailable"
    }
}
