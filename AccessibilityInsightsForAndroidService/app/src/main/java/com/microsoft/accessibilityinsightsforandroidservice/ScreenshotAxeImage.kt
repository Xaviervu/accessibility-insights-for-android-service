// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.util.Base64
import com.deque.axe.android.colorcontrast.AxeColor
import com.deque.axe.android.colorcontrast.AxeImage
import com.deque.axe.android.wrappers.AxeRect

class ScreenshotAxeImage(
    private val screenshot: Bitmap,
    private val byteArrayOutputStreamProvider: ByteArrayOutputStreamProvider
) : AxeImage() {
    private val frameRect: AxeRect

    init {
        frameRect = AxeRect(0, screenshot.getWidth() - 1, 0, screenshot.getHeight() - 1)
    }

    override fun frame(): AxeRect {
        return frameRect
    }

    override fun pixel(x: Int, y: Int): AxeColor {
        val color = AxeColor(this.screenshot.getPixel(x, y))
        return color
    }

    override fun toBase64Png(): String? {
        val byteArrayOutputStream = byteArrayOutputStreamProvider.get()
        screenshot.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
