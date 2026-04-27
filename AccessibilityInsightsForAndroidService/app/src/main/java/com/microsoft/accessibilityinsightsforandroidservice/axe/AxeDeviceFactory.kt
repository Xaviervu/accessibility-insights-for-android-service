// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice.axe

import android.os.Build
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeDevice
import com.microsoft.accessibilityinsightsforandroidservice.DeviceConfigFactory
import java.util.function.Supplier

class AxeDeviceFactory(
    private val deviceConfigFactory: DeviceConfigFactory,
    private val displayMetricsSupplier: Supplier<DisplayMetrics>
) {
    fun createAxeDevice(rootNode: AccessibilityNodeInfo?): AxeDevice {
        val displayMetrics = displayMetricsSupplier.get()
        val compoundVersion = Build.VERSION.RELEASE + " API Level " + Build.VERSION.SDK_INT
        val deviceConfig = deviceConfigFactory.getDeviceConfig(rootNode)
        return AxeDevice(
            displayMetrics.density,
            deviceConfig.deviceName,
            compoundVersion,
            displayMetrics.heightPixels,
            displayMetrics.widthPixels
        )
    }
}
