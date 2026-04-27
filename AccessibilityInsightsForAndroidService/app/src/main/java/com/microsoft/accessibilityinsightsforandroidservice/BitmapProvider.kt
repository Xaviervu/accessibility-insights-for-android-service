// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap

class BitmapProvider {
    fun createBitmap(
        width: Int,
        height: Int,
        config: Bitmap.Config,
    ): Bitmap = Bitmap.createBitmap(width, height, config)
}
