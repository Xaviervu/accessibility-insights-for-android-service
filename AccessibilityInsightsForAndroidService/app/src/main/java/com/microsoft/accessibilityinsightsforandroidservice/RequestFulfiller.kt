// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal

interface RequestFulfiller {
    @Throws(Exception::class)
    fun fulfillRequest(cancellationSignal: CancellationSignal): String
}
