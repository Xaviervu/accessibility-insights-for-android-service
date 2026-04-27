// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.function.Supplier

class LayoutParamGenerator(
    private val displayMetricsSupplier: Supplier<DisplayMetrics>,
) {
    fun get(): WindowManager.LayoutParams {
        val displayMetrics = displayMetricsSupplier.get()
        val params =
            WindowManager.LayoutParams(
                displayMetrics.widthPixels,
                displayMetrics.heightPixels,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                (
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                ),
                PixelFormat.TRANSLUCENT,
            )

        return params
    }
}
