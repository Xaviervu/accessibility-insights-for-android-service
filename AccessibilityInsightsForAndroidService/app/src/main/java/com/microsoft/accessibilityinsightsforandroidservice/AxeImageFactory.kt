// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import com.deque.axe.android.colorcontrast.AxeImage

class AxeImageFactory(private val byteArrayOutputStreamProvider: ByteArrayOutputStreamProvider) {
    fun createAxeImage(screenshot: Bitmap?): AxeImage? {
        if (screenshot == null) {
            return null
        }

        return ScreenshotAxeImage(screenshot, byteArrayOutputStreamProvider)
    }
}
