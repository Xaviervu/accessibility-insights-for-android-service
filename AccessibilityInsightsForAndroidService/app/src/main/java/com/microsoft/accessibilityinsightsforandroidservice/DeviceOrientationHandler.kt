// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import java.util.function.Consumer

class DeviceOrientationHandler(
    private var orientation: Int,
) {
    private val onOrientationChangedListeners: ArrayList<Consumer<Int?>?>

    init {
        this.onOrientationChangedListeners = ArrayList<Consumer<Int?>?>()
    }

    fun subscribe(listener: Consumer<Int?>?) {
        this.onOrientationChangedListeners.add(listener)
    }

    fun setOrientation(orientation: Int) {
        if (this.orientation == orientation) {
            return
        }

        this.orientation = orientation
        this.emitChanged(orientation)
    }

    private fun emitChanged(orientation: Int) {
        this.onOrientationChangedListeners.forEach(
            Consumer { listener: Consumer<Int?>? ->
                listener!!.accept(orientation)
            },
        )
    }
}
