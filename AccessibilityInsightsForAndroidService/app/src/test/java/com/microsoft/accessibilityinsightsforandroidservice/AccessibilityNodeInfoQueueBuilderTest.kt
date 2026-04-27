// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.Queue

@RunWith(MockitoJUnitRunner::class)
class AccessibilityNodeInfoQueueBuilderTest {
    @Mock
    var rootNode: AccessibilityNodeInfo? = null

    @Mock
    var childNode0: AccessibilityNodeInfo? = null

    @Mock
    var childNode1: AccessibilityNodeInfo? = null

    @Mock
    var grandchildNode: AccessibilityNodeInfo? = null

    lateinit var testSubject: AccessibilityNodeInfoQueueBuilder

    @Before
    fun prepare() {
        testSubject = AccessibilityNodeInfoQueueBuilder()
    }

    @Test
    fun buildEmptyQueue() {
        val queue: Queue<OrderedValue<AccessibilityNodeInfo>> =
            testSubject.buildPriorityQueue(null)
        Assert.assertNotNull(queue)
        Assert.assertTrue(queue.isEmpty())
    }

    @Test
    fun buildSingleNodeQueue() {
        val queue: Queue<OrderedValue<AccessibilityNodeInfo>> =
            testSubject.buildPriorityQueue(rootNode)
        Assert.assertNotNull(queue)
        Assert.assertEquals(queue.size.toLong(), 1)

        assertNextQueueItemEquals(queue, rootNode, Long.Companion.MAX_VALUE)
    }

    @Test
    fun buildQueueWithChildren() {
        createChildren()

        val queue: Queue<OrderedValue<AccessibilityNodeInfo>> =
            testSubject.buildPriorityQueue(rootNode)

        val rootOrder = Long.Companion.MAX_VALUE
        val childOrder = rootOrder - 1
        val grandchildOrder = rootOrder - 2

        Assert.assertEquals(queue.size.toLong(), 4)
        assertNextQueueItemEquals(queue, grandchildNode, grandchildOrder)
        // Since the child nodes have the same priority, they can appear here in any order,
        // so check for both ordering possibilities
        if (queue.peek()!!.value === childNode0) {
            assertNextQueueItemEquals(queue, childNode0, childOrder)
            assertNextQueueItemEquals(queue, childNode1, childOrder)
        } else {
            assertNextQueueItemEquals(queue, childNode1, childOrder)
            assertNextQueueItemEquals(queue, childNode0, childOrder)
        }
        assertNextQueueItemEquals(queue, rootNode, rootOrder)
    }

    @Test
    fun buildQueueWithLabelNodes() {
        createChildren()

        Mockito.`when`<AccessibilityNodeInfo?>(childNode0?.getLabelFor()).thenReturn(childNode1)

        val queue: Queue<OrderedValue<AccessibilityNodeInfo>> =
            testSubject.buildPriorityQueue(rootNode)

        val rootOrder = Long.Companion.MAX_VALUE
        val child0Order = (rootOrder - 1) / 2
        val child1Order = rootOrder - 1
        val grandchildOrder = child0Order - 1

        Assert.assertEquals(queue.size.toLong(), 4)
        assertNextQueueItemEquals(queue, grandchildNode, grandchildOrder)
        assertNextQueueItemEquals(queue, childNode0, child0Order)
        assertNextQueueItemEquals(queue, childNode1, child1Order)
        assertNextQueueItemEquals(queue, rootNode, rootOrder)
    }

    fun createChildren() {
        Mockito.`when`<Int?>(rootNode?.childCount).thenReturn(2)
        Mockito.`when`<AccessibilityNodeInfo?>(rootNode?.getChild(0)).thenReturn(childNode0)
        Mockito.`when`<AccessibilityNodeInfo?>(rootNode?.getChild(1)).thenReturn(childNode1)

        Mockito.`when`<Int?>(childNode0?.childCount).thenReturn(1)
        Mockito.`when`<AccessibilityNodeInfo?>(childNode0!!.getChild(0)).thenReturn(grandchildNode)
    }

    fun assertNextQueueItemEquals(
        queue: Queue<OrderedValue<AccessibilityNodeInfo>>,
        node: AccessibilityNodeInfo?,
        priority: Long,
    ) {
        val nextItem = queue.poll()
        Assert.assertNotNull(nextItem)
        Assert.assertEquals(nextItem!!.value, node)
        Assert.assertEquals(nextItem.order, priority)
    }
}
