// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo

class EventHelper(
    private val swapper: ThreadSafeSwapper<AccessibilityNodeInfo?>,
) {
    fun recordEvent(source: AccessibilityNodeInfo?) {
        if (source != null) {
            val lastSource = swapper.swap(source)
            if (lastSource != null) {
                lastSource.recycle()
            }
        }
    }

    fun claimLastSource(): AccessibilityNodeInfo? = swapper.swap(null)

    fun restoreLastSource(previousSource: AccessibilityNodeInfo?): Boolean = swapper.setIfCurrentlyNull(previousSource)
}
