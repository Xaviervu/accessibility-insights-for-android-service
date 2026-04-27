// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeView
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeRectProvider

class NodeViewBuilderFactory {
    // The reason we need something called BuilderFactory is because axe-android requires us to
    // implement the AxeView.builder interface, which we do in NodeViewBuilder. This is a factory
    // for that class.
    fun createNodeViewBuilder(
        node: AccessibilityNodeInfo, children: MutableList<AxeView>, labeledBy: AxeView?
    ): NodeViewBuilder {
        return NodeViewBuilder(node, children, labeledBy, AxeRectProvider())
    }
}
