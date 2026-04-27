// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.View

object OffsetHelper {
    @JvmStatic
    fun getYOffset(view: View): Int {
        var offset = 0
        val resourceId = view.getResources().getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            offset = view.getResources().getDimensionPixelSize(resourceId)
        }
        // divide by 2 to center
        offset = offset / 2
        return offset
    }
}
