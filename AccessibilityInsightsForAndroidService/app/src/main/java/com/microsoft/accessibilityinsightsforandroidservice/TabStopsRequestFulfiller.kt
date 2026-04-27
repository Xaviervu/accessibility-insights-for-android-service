// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal

class TabStopsRequestFulfiller(
    private val focusVisualizationStateManager: FocusVisualizationStateManager,
    private val requestValue: Boolean
) : RequestFulfiller {
    override fun fulfillRequest(cancellationSignal: CancellationSignal): String {
        focusVisualizationStateManager.state = requestValue
        return ""
    }
}
