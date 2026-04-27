// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Arrays
import java.util.function.Consumer

class AccessibilityEventDispatcher {
    private var previousPackageName: CharSequence? = null

    private val onAppChangedListeners: ArrayList<Consumer<AccessibilityNodeInfo?>?>
    private val onFocusEventListeners: ArrayList<Consumer<AccessibilityEvent?>?>
    private val onRedrawEventListeners: ArrayList<Consumer<AccessibilityEvent?>?>

    init {
        onAppChangedListeners = ArrayList<Consumer<AccessibilityNodeInfo?>?>()
        onFocusEventListeners = ArrayList<Consumer<AccessibilityEvent?>?>()
        onRedrawEventListeners = ArrayList<Consumer<AccessibilityEvent?>?>()
    }

    fun onAccessibilityEvent(event: AccessibilityEvent, rootNode: AccessibilityNodeInfo?) {
        val eventType = event.getEventType()

        if (rootNode != null
            && (previousPackageName == null
                    || previousPackageName != rootNode.getPackageName())
        ) {
            previousPackageName = rootNode.getPackageName()
            this.callListeners<AccessibilityNodeInfo?>(onAppChangedListeners, rootNode)
        }

        if (isFocusEvent(eventType)) {
            this.callListeners<AccessibilityEvent?>(onFocusEventListeners, event)
            return
        }

        if (isRedrawEvent(eventType)) {
            this.callListeners<AccessibilityEvent?>(onRedrawEventListeners, event)
            return
        }
    }

    fun addOnFocusEventListener(listener: Consumer<AccessibilityEvent?>?) {
        onFocusEventListeners.add(listener)
    }

    fun addOnRedrawEventListener(listener: Consumer<AccessibilityEvent?>?) {
        onRedrawEventListeners.add(listener)
    }

    fun addOnAppChangedListener(listener: Consumer<AccessibilityNodeInfo?>?) {
        onAppChangedListeners.add(listener)
    }

    private fun isFocusEvent(eventType: Int): Boolean {
        return eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED
    }

    private fun isRedrawEvent(eventType: Int): Boolean {
        return redrawEventTypes.contains(eventType)
    }

    private fun <T> callListeners(listeners: ArrayList<Consumer<T?>?>, newValue: T?) {
        listeners.forEach(
            Consumer { listener: Consumer<T?>? ->
                listener!!.accept(newValue)
            })
    }

    companion object {
        private const val TAG = "AccessibilityEventDispatcher"
        @JvmField
        var redrawEventTypes: MutableList<Int?> = Arrays.asList<Int?>(
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED
        )
    }
}
