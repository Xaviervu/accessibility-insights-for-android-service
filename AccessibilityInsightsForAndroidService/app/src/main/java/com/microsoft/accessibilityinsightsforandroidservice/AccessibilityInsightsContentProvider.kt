// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor

class AccessibilityInsightsContentProvider : ContentProvider() {
    private var requestDispatcher: SynchronizedRequestDispatcher? = null
    private var tempFileProvider: TempFileProvider? = null

    override fun onCreate(): Boolean {
        return onCreate(
            SynchronizedRequestDispatcher.SharedInstance, TempFileProvider(this.context ?: return false)
        )
    }

    fun onCreate(
        requestDispatcher: SynchronizedRequestDispatcher, tempFileProvider: TempFileProvider
    ): Boolean {
        this.requestDispatcher = requestDispatcher
        this.tempFileProvider = tempFileProvider
        return true
    }

    override fun query(
        uri: Uri,
        strings: Array<String>?,
        s: String?,
        strings1: Array<String?>?,
        s1: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String?>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        contentValues: ContentValues?,
        s: String?,
        strings: Array<String?>?
    ): Int {
        return 0
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        verifyCallerPermissions()

        val output = Bundle()

        try {
            val response = requestDispatcher!!.request("/" + method, CancellationSignal())
            output.putString("response", response)
        } catch (e: Exception) {
            output.putString("response", e.toString())
        }

        return output
    }

    override fun openFile(
        uri: Uri, mode: String, signal: CancellationSignal?
    ): ParcelFileDescriptor? {
        var signal = signal
        verifyCallerPermissions()

        if (signal == null) {
            signal = CancellationSignal()
        }

        val method = uri.getPath()

        var response: String?
        try {
            response = requestDispatcher!!.request(method!!, signal)
        } catch (e: Exception) {
            response = e.toString()
        }

        try {
            val file =
                ParcelFileDescriptor.open(
                    tempFileProvider!!.createTempFileWithContents(response),
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
            return file
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private val AID_SHELL = 2000 // from android_filesystem_config.h

    private fun verifyCallerPermissions() {
        if (Binder.getCallingUid() != AID_SHELL) {
            throw SecurityException("This provider may only be queried via adb's shell user")
        }
    }
}
