// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice.axe

import android.util.DisplayMetrics
import com.microsoft.accessibilityinsightsforandroidservice.AccessibilityNodeInfoQueueBuilder
import com.microsoft.accessibilityinsightsforandroidservice.ByteArrayOutputStreamProvider
import com.microsoft.accessibilityinsightsforandroidservice.DeviceConfigFactory
import com.microsoft.accessibilityinsightsforandroidservice.NodeViewBuilderFactory
import java.util.function.Supplier

object AxeScannerFactory {
    fun createAxeScanner(
        deviceConfigFactory: DeviceConfigFactory,
        displayMetricsSupplier: Supplier<DisplayMetrics>,
    ): AxeScanner {
        val axeViewsFactory =
            AxeViewsFactory(NodeViewBuilderFactory(), AccessibilityNodeInfoQueueBuilder())
        val axeImageFactory =
            AxeImageFactory(ByteArrayOutputStreamProvider())
        val axeDeviceFactory =
            AxeDeviceFactory(deviceConfigFactory, displayMetricsSupplier)
        val axeContextFactory =
            AxeContextFactory(axeImageFactory, axeViewsFactory, axeDeviceFactory)
        val axeRunnerFactory = AxeRunnerFactory()

        return AxeScanner(axeRunnerFactory, axeContextFactory)
    }
}
