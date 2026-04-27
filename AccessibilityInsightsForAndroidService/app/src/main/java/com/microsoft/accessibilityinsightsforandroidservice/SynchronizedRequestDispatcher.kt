// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal
import androidx.annotation.AnyThread

class SynchronizedRequestDispatcher {
    private var underlyingDispatcher: RequestDispatcher? = null
    private val lock = Any()
    private var teardownSignal: CancellationSignal? = CancellationSignal()

    @AnyThread
    fun setup(instance: RequestDispatcher) {
        synchronized(lock) {
            if (this.underlyingDispatcher != null) {
                throw RuntimeException("Attempt to double-initialize instance")
            }
            this.teardownSignal = CancellationSignal()
            this.underlyingDispatcher = instance
        }
    }

    @AnyThread
    fun teardown() {
        val teardownSignal = this.teardownSignal
        if (teardownSignal != null) {
            teardownSignal.cancel()
        }

        synchronized(lock) {
            this.underlyingDispatcher = null
            this.teardownSignal = null
        }
    }

    @AnyThread
    @Throws(Exception::class)
    fun request(method: String, cancellationSignal: CancellationSignal): String? {
        val combinedCancellationSignal = CancellationSignal()

        synchronized(lock) {
            if (underlyingDispatcher == null) {
                throw Exception("Service is not running")
            }
            teardownSignal!!.setOnCancelListener(CancellationSignal.OnCancelListener { combinedCancellationSignal.cancel() })
            cancellationSignal.setOnCancelListener(CancellationSignal.OnCancelListener { combinedCancellationSignal.cancel() })
            combinedCancellationSignal.throwIfCanceled()
            return underlyingDispatcher!!.request(method, combinedCancellationSignal)
        }
    }

    companion object {
        val SharedInstance: SynchronizedRequestDispatcher = SynchronizedRequestDispatcher()
    }
}
