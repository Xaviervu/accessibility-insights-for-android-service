// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice.atfa

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.Parameters
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage
import java.util.Arrays

class ATFAScanner(private val context: Context) {
    private val relevantResultTypes = arrayOf<AccessibilityCheckResultType?>(
        AccessibilityCheckResultType.ERROR,
        AccessibilityCheckResultType.INFO,
        AccessibilityCheckResultType.WARNING,
        AccessibilityCheckResultType.RESOLVED,
        AccessibilityCheckResultType.NOT_RUN
    )

    fun scanWithATFA(
        rootNode: AccessibilityNodeInfo, screenshot: BitmapImage?
    ): MutableList<AccessibilityHierarchyCheckResult?> {
        val parameters = Parameters()
        parameters.setSaveViewImages(true)
        parameters.putCustomTouchTargetSize(44) // default is 48 but min size as defined by WCAG is 44
        parameters.putScreenCapture(screenshot)

        val checks =
            AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
                AccessibilityCheckPreset.LATEST
            )
        val hierarchy =
            AccessibilityHierarchyAndroid.newBuilder(rootNode, this.context).build()
        val results: MutableList<AccessibilityHierarchyCheckResult?> =
            ArrayList<AccessibilityHierarchyCheckResult?>()

        for (check in checks) {
            results.addAll(check.runCheckOnHierarchy(hierarchy, null, parameters))
        }

        return AccessibilityCheckResultUtils.getResultsForTypes<AccessibilityHierarchyCheckResult?>(
            results,
            HashSet<AccessibilityCheckResultType?>(Arrays.asList<AccessibilityCheckResultType?>(*relevantResultTypes))
        )
    }
}
