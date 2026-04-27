// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice.axe

import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeView
import com.microsoft.accessibilityinsightsforandroidservice.AccessibilityNodeInfoQueueBuilder
import com.microsoft.accessibilityinsightsforandroidservice.NodeViewBuilderFactory
import com.microsoft.accessibilityinsightsforandroidservice.OrderedValue
import com.microsoft.accessibilityinsightsforandroidservice.ViewChangedException
import java.util.Hashtable
import java.util.Queue
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.get

class AxeViewsFactory(
    var nodeViewBuilderFactory: NodeViewBuilderFactory,
    var queueBuilder: AccessibilityNodeInfoQueueBuilder
) {
    lateinit var axeMap: MutableMap<AccessibilityNodeInfo, AxeView>

    @Throws(ViewChangedException::class)
    fun createAxeViews(rootNode: AccessibilityNodeInfo): AxeView? {
        return buildAxeViewsWithRetries(rootNode, maxRetries)
    }

    @Throws(ViewChangedException::class)
    private fun buildAxeViewsWithRetries(rootNode: AccessibilityNodeInfo, retries: Int): AxeView? {
        val queue: Queue<OrderedValue<AccessibilityNodeInfo>> =
            queueBuilder.buildPriorityQueue(rootNode)
        axeMap = Hashtable()

        try {
            return buildAxeViews(queue, rootNode)
        } catch (e: ViewChangedException) {
            if (retries > 0) {
                rootNode.refresh()
                return buildAxeViewsWithRetries(rootNode, retries - 1)
            } else {
                throw ViewChangedException("Failed after $maxRetries attempts.")
            }
        } finally {
            recycleAllNodes(rootNode, queue)
        }
    }

    @Throws(ViewChangedException::class)
    private fun buildAxeViews(
        queue: Queue<OrderedValue<AccessibilityNodeInfo>>, rootNode: AccessibilityNodeInfo?
    ): AxeView? {
        var nextOrderedNode: OrderedValue<AccessibilityNodeInfo>?

        while ((queue.poll().also { nextOrderedNode = it }) != null) {
            val node = nextOrderedNode?.value ?: continue
            val children = getChildViews(node)
            val labeledByView = getLabeledByView(node)
            val nodeView =
                this.nodeViewBuilderFactory.createNodeViewBuilder(node,
                    children, labeledByView)
                    .build()
            axeMap[node] = nodeView
        }

        return axeMap[rootNode]
    }

    private fun getLabeledByView(node: AccessibilityNodeInfo): AxeView? {
        var labeledByView: AxeView? = null
        val labeledByNode = node.getLabeledBy()
        if (labeledByNode != null) {
            labeledByView = axeMap[labeledByNode]
        }

        return labeledByView
    }

    @Throws(ViewChangedException::class)
    private fun getChildViews(node: AccessibilityNodeInfo): MutableList<AxeView> {
        val childCount = node.childCount
        val children: MutableList<AxeView> = ArrayList(childCount)

        for (loop in 0..<childCount) {
            val child = node.getChild(loop) ?: throw ViewChangedException()
            axeMap[child]?.let { children.add(it) }
        }

        return children
    }

    private fun recycleAllNodes(
        rootNode: AccessibilityNodeInfo?, queue: Queue<OrderedValue<AccessibilityNodeInfo>>
    ) {
        val allNodes: MutableSet<AccessibilityNodeInfo> =
            HashSet<AccessibilityNodeInfo>(axeMap.keys)
        for (orderedNode in queue) {
            allNodes.add(orderedNode.value)
        }

        for (node in allNodes) {
            if (node !== rootNode && node.getClassName() != null) {
                node.recycle()
            }
        }
    }

    companion object {
        private const val maxRetries = 5
    }
}
