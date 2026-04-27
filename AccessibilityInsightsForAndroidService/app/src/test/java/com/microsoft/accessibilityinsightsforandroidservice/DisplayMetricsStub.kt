// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.util.DisplayMetrics

class DisplayMetricsStub : DisplayMetrics() {
    var densityDpi: Int = 111
    var heightPixels: Int = 222
    var widthPixels: Int = 333
}
