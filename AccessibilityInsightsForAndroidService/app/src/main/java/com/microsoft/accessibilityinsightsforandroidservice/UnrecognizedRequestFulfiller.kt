// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal

class UnrecognizedRequestFulfiller(
    private val requestMethod: String,
) : RequestFulfiller {
    override fun fulfillRequest(cancellationSignal: CancellationSignal): String =
        throw RuntimeException("Unrecognized request: $requestMethod")
}
