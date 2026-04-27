// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import java.util.function.Consumer

class FocusVisualizationStateManager {
    private var enabled = false
    private val onChangedListeners: ArrayList<Consumer<Boolean?>?>

    init {
        onChangedListeners = ArrayList<Consumer<Boolean?>?>()
    }

    fun subscribe(listener: Consumer<Boolean?>?) {
        onChangedListeners.add(listener)
    }

    var state: Boolean
        get() = this.enabled
        set(enabled) {
            if (this.enabled == enabled) {
                return
            }

            this.enabled = enabled
            this.emitChanged(enabled)
        }

    private fun emitChanged(enabled: Boolean) {
        onChangedListeners.forEach(
            Consumer { listener: Consumer<Boolean?>? ->
                listener!!.accept(enabled)
            },
        )
    }
}
