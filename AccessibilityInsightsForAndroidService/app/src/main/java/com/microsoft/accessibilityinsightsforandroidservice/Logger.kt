// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.android.apps.common.testing.accessibility.framework.BuildConfig

object Logger {
    @JvmField
    @VisibleForTesting
    var ENABLE_LOGGING: Boolean = BuildConfig.DEBUG

    @JvmStatic
    fun logVerbose(tag: String?, message: String) {
        if (ENABLE_LOGGING) {
            Log.v(tag, message)
        }
    }

    @JvmStatic
    fun logDebug(tag: String?, message: String) {
        if (ENABLE_LOGGING) {
            Log.d(tag, message)
        }
    }

    @JvmStatic
    fun logError(tag: String?, message: String) {
        if (ENABLE_LOGGING) {
            Log.e(tag, message)
        }
    }

    @JvmStatic
    fun logInfo(tag: String?, message: String) {
        if (ENABLE_LOGGING) {
            Log.i(tag, message)
        }
    }

    @JvmStatic
    fun logWarning(tag: String?, message: String) {
        if (ENABLE_LOGGING) {
            Log.w(tag, message)
        }
    }
}
