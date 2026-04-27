// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

object DisplayMetricsHelper {
    @JvmStatic
    fun getRealDisplayMetrics(context: Context): DisplayMetrics {
        val displayMetrics = Resources.getSystem().displayMetrics // Default values

        val display = getDefaultDisplay(context)

        display.getRealMetrics(displayMetrics)
        return displayMetrics
    }

    private fun getDefaultDisplay(context: Context): Display {
        return (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).getDefaultDisplay()
    }
}
