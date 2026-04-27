// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.util.DisplayMetrics
import java.util.function.Consumer

class OnScreenshotAvailableProvider {
    fun getOnScreenshotAvailable(
        metrics: DisplayMetrics,
        bitmapProvider: BitmapProvider,
        bitmapConsumer: Consumer<Bitmap>,
    ): OnScreenshotAvailable = OnScreenshotAvailable(metrics, bitmapProvider, bitmapConsumer)
}
