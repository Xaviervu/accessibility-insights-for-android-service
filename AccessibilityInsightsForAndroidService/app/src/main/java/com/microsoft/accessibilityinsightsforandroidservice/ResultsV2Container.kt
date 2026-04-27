// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import com.deque.axe.android.AxeResult
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult

class ResultsV2Container {
    @JvmField
    var ATFAResults: List<AccessibilityHierarchyCheckResult>? = null
    @JvmField
    var AxeResult: AxeResult? = null
}
