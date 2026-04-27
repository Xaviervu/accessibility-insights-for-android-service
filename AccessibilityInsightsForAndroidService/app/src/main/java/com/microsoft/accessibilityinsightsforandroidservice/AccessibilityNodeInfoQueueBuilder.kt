// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo
import java.util.PriorityQueue
import java.util.Queue

class AccessibilityNodeInfoQueueBuilder {
    fun buildPriorityQueue(rootNode: AccessibilityNodeInfo?): Queue<OrderedValue<AccessibilityNodeInfo>> {
        val queue = PriorityQueue<OrderedValue<AccessibilityNodeInfo>>()
        if(rootNode == null) return queue
        recursivelyEnqueueNodes(queue, rootNode, Long.Companion.MAX_VALUE)
        return queue
    }

    private fun recursivelyEnqueueNodes(
        queue: PriorityQueue<OrderedValue<AccessibilityNodeInfo>>,
        node: AccessibilityNodeInfo,
        order: Long,
    ) {
        // The AxeView object requires that we create the AxeView
        // objects for both child nodes and for any labeledBy nodes. Child nodes use
        // easily predictable rules, but labeledBy nodes are less structured. We use
        // a PriorityQueue where children are given a slightly higher priority than
        // the parent, but nodes that are used as labels are given a significantly
        // higher priority. This will work for most "typical" cases, but not the case
        // where a node's labeledBy value points to a direct ancestor. That scenario
        // could require changes to the AxeView class, which is immutable after construction.

        var order1 = order

        if (node.getLabelFor() != null) {
            order1 /= 2
        }

        queue.add(OrderedValue(node, order1))

        val childCount = node.childCount

        for (loop in 0..<childCount) {
            recursivelyEnqueueNodes(queue, node.getChild(loop), order1 - 1)
        }
    }
}
