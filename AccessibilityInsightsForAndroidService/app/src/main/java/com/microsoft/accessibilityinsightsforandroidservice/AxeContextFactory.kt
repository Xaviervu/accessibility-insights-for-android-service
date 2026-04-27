// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeContext
import com.deque.axe.android.wrappers.AxeEventStream

class AxeContextFactory(
    private val axeImageFactory: AxeImageFactory,
    private val axeViewsFactory: AxeViewsFactory,
    private val axeDeviceFactory: AxeDeviceFactory
) {
    @Throws(ViewChangedException::class)
    fun createAxeContext(rootNode: AccessibilityNodeInfo, screenshot: Bitmap): AxeContext {
        val axeView = axeViewsFactory.createAxeViews(rootNode)
        val axeDevice = axeDeviceFactory.createAxeDevice(rootNode)
        val axeImage = axeImageFactory.createAxeImage(screenshot)
        val axeEventStream = AxeEventStream()
        return AxeContext(axeView, axeDevice, axeImage, axeEventStream)
    }
}
