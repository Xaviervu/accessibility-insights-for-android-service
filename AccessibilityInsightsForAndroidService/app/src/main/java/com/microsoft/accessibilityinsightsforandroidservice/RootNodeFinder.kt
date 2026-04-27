// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo

class RootNodeFinder {
    fun getRootNodeFromSource(source: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var rootNode: AccessibilityNodeInfo? = null

        if (source != null) {
            var currentNode: AccessibilityNodeInfo? = source

            while (true) {
                val parent = currentNode!!.getParent()

                if (parent == null) {
                    rootNode = currentNode
                    break
                }

                if (source !== currentNode) { // Don't recycle the source!
                    currentNode.recycle()
                }
                currentNode = parent
            }
        }

        return rootNode
    }
}
