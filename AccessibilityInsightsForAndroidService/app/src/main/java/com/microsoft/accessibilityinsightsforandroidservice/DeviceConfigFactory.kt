// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo

class DeviceConfigFactory @JvmOverloads constructor(private val buildModel: String? = Build.MODEL) {
    private val serviceVersion = "0.1.0"

    fun getDeviceConfig(rootNode: AccessibilityNodeInfo?): DeviceConfig {
        val packageName = getPackageNameFromAccessibilityNode(rootNode)

        return DeviceConfig(buildModel, packageName, serviceVersion)
    }

    private fun getPackageNameFromAccessibilityNode(rootNode: AccessibilityNodeInfo?): String {
        var packageName = "No application detected"
        if (rootNode != null) {
            val sequence = rootNode.getPackageName()
            if (sequence != null) {
                packageName = sequence.toString()
            }
        }
        return packageName
    }
}
