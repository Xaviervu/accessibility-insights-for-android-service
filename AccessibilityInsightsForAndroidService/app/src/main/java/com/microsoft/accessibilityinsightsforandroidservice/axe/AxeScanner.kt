// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice.axe

import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeResult
import com.microsoft.accessibilityinsightsforandroidservice.ViewChangedException

class AxeScanner(
    private val axeRunnerFactory: AxeRunnerFactory,
    private val axeContextFactory: AxeContextFactory
) {
    @Throws(ViewChangedException::class)
    fun scanWithAxe(rootNode: AccessibilityNodeInfo, screenshot: Bitmap): AxeResult {
        val axe = axeRunnerFactory.createAxeRunner()
        val axeContext = axeContextFactory.createAxeContext(rootNode, screenshot)
        return axe.run(axeContext)
    }
}
