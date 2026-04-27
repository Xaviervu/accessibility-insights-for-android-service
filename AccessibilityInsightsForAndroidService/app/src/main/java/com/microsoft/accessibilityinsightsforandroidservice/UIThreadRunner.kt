// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.Handler
import android.os.Looper

class UIThreadRunner {
    fun run(runnable: Runnable) {
        Handler(Looper.getMainLooper()).post(runnable)
    }
}
