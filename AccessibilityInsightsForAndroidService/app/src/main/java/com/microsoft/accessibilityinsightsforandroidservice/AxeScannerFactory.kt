// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.util.DisplayMetrics
import java.util.function.Supplier

object AxeScannerFactory {
    fun createAxeScanner(
        deviceConfigFactory: DeviceConfigFactory,
        displayMetricsSupplier: Supplier<DisplayMetrics>
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
