// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

class ThreadSafeSwapper<T> {
    private val lock_object = Any()
    private var currentObject: T? = null

    fun swap(newObject: T?): T? {
        synchronized(lock_object) {
            val oldObject = currentObject
            currentObject = newObject
            return oldObject
        }
    }

    fun setIfCurrentlyNull(newObject: T?): Boolean {
        synchronized(lock_object) {
            val oldObject = currentObject
            if (oldObject != null) {
                return false
            }

            currentObject = newObject
            return true
        }
    }
}
