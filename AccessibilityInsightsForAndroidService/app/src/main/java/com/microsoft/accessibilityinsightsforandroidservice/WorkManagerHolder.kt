// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import java.util.WeakHashMap

object WorkManagerHolder {
    private val LockObject = Any()
    private val ContextToManagerMap = WeakHashMap<Context?, WorkManager?>()

    fun getWorkManager(context: Context): WorkManager {
        synchronized(LockObject) {
            var managerForContext = ContextToManagerMap.get(context)
            if (managerForContext == null) {
                try {
                    managerForContext = WorkManager.getInstance(context)
                } catch (e: IllegalStateException) {
                    try {
                        WorkManager.initialize(context, Configuration.Builder().build())
                    } catch (e2: IllegalStateException) {
                        // In case it was initialized between getInstance and initialize calls
                    }
                    managerForContext = WorkManager.getInstance(context)
                }
                ContextToManagerMap.put(context, managerForContext)
            }
            return managerForContext
        }
    }
}
