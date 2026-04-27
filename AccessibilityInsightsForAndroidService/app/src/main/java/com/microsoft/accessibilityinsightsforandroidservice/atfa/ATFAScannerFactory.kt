// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice.atfa

import android.content.Context

object ATFAScannerFactory {
    @JvmStatic
    fun createATFAScanner(context: Context): ATFAScanner = ATFAScanner(context)
}
