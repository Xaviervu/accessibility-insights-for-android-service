// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeView
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeViewsFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer
import java.util.LinkedList
import java.util.Queue

@RunWith(MockitoJUnitRunner::class)
class AxeViewsFactoryTest {
    @Mock
    var nodeViewBuilderFactoryMock: NodeViewBuilderFactory? = null

    @Mock
    var queueBuilderMock: AccessibilityNodeInfoQueueBuilder? = null

    @Mock
    var rootNodeBuilder: NodeViewBuilder? = null

    @Mock
    var childNodeBuilder: NodeViewBuilder? = null

    @Mock
    var labelNodeBuilder: NodeViewBuilder? = null

    @Mock
    var rootNodeMock: AccessibilityNodeInfo? = null

    @Mock
    var childNodeMock: AccessibilityNodeInfo? = null

    @Mock
    var labelNodeMock: AccessibilityNodeInfo? = null

    @Mock
    var rootViewMock: AxeView? = null

    @Mock
    var childViewMock: AxeView? = null

    @Mock
    var labelViewMock: AxeView? = null

    var queue: Queue<OrderedValue<AccessibilityNodeInfo?>?>? = null

    var testSubject: AxeViewsFactory? = null

    val nodeClassName: String = "node class name"

    @Before
    fun prepare() {
        setupNodeViewCreation(rootNodeBuilder, rootNodeMock, rootViewMock)
        setupNodeViewCreation(childNodeBuilder, childNodeMock, childViewMock)
        setupNodeViewCreation(labelNodeBuilder, labelNodeMock, labelViewMock)

        queue = LinkedList<OrderedValue<AccessibilityNodeInfo?>?>()
        Mockito.`when`<Queue<OrderedValue<AccessibilityNodeInfo>>>(
            queueBuilderMock!!.buildPriorityQueue(
                rootNodeMock!!
            )
        ).thenReturn(queue)

        Mockito.`when`<CharSequence?>(childNodeMock!!.getClassName()).thenReturn(nodeClassName)
        Mockito.`when`<CharSequence?>(labelNodeMock!!.getClassName()).thenReturn(nodeClassName)

        testSubject = AxeViewsFactory(nodeViewBuilderFactoryMock!!, queueBuilderMock!!)
    }

    @Test
    @Throws(ViewChangedException::class)
    fun axeViewIsNotNull() {
        enqueueNode(rootNodeMock)

        Assert.assertNotNull(testSubject!!.createAxeViews(rootNodeMock!!))
    }

    @Test
    @Throws(ViewChangedException::class)
    fun createsAxeViewWithoutChildren() {
        enqueueNode(rootNodeMock)

        val axeView = testSubject!!.createAxeViews(rootNodeMock!!)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito.verify<NodeViewBuilderFactory?>(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(rootNodeMock),
                ArgumentMatchers.eq<ArrayList<AxeView>?>(
                    ArrayList<AxeView?>()
                ),
                ArgumentMatchers.eq<AxeView?>(null)
            )
    }

    @Test
    @Throws(ViewChangedException::class)
    fun createsAxeViewWithChildNode() {
        Mockito.`when`<Int?>(rootNodeMock!!.getChildCount()).thenReturn(1)
        Mockito.`when`<AccessibilityNodeInfo?>(rootNodeMock!!.getChild(0)).thenReturn(childNodeMock)

        enqueueNode(childNodeMock)
        enqueueNode(rootNodeMock)

        val axeView = testSubject!!.createAxeViews(rootNodeMock!!)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        val children = ArrayList<AxeView?>()
        children.add(childViewMock)

        Mockito.verify<NodeViewBuilderFactory?>(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(rootNodeMock),
                ArgumentMatchers.eq<ArrayList<AxeView?>?>(children),
                ArgumentMatchers.eq<AxeView?>(null)
            )
        Mockito.verify<NodeViewBuilderFactory?>(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(childNodeMock),
                ArgumentMatchers.eq<ArrayList<AxeView>?>(
                    ArrayList<AxeView?>()
                ),
                ArgumentMatchers.eq<AxeView?>(null)
            )
    }

    @Test
    @Throws(ViewChangedException::class)
    fun createsAxeViewWithLabeledByNode() {
        Mockito.`when`<AccessibilityNodeInfo?>(rootNodeMock!!.getLabeledBy())
            .thenReturn(labelNodeMock)

        enqueueNode(labelNodeMock)
        enqueueNode(rootNodeMock)

        val axeView = testSubject!!.createAxeViews(rootNodeMock!!)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito.verify<NodeViewBuilderFactory?>(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(rootNodeMock),
                ArgumentMatchers.eq<ArrayList<AxeView>?>(
                    ArrayList<AxeView?>()
                ),
                ArgumentMatchers.eq<AxeView?>(labelViewMock)
            )
        Mockito.verify<NodeViewBuilderFactory?>(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(labelNodeMock),
                ArgumentMatchers.eq<ArrayList<AxeView>?>(
                    ArrayList<AxeView?>()
                ),
                ArgumentMatchers.eq<AxeView?>(null)
            )
    }

    @Test
    @Throws(ViewChangedException::class)
    fun refreshAndRetryIfViewChanged() {
        setupViewChangedScenario(true, false)

        val axeView = testSubject!!.createAxeViews(rootNodeMock!!)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.times(1)).refresh()
        Mockito.verify<AccessibilityNodeInfoQueueBuilder?>(queueBuilderMock, Mockito.times(2))
            .buildPriorityQueue(rootNodeMock!!)
        Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.times(2)).getChildCount()
        Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.times(2)).getChild(0)
    }

    @Test
    fun retriesFiveTimes() {
        setupViewChangedScenario(false, false)

        val numRetries = 5

        try {
            testSubject!!.createAxeViews(rootNodeMock!!)
            Assert.fail("Expected createAxeViews to throw exception")
        } catch (e: ViewChangedException) {
            Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.times(numRetries))
                .refresh()
            Mockito.verify<AccessibilityNodeInfoQueueBuilder?>(
                queueBuilderMock,
                Mockito.times(numRetries + 1)
            ).buildPriorityQueue(rootNodeMock!!)
            Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.times(numRetries + 1))
                .getChildCount()
            Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.times(numRetries + 1))
                .getChild(0)
        }
    }

    @Test
    @Throws(ViewChangedException::class)
    fun recyclesNodesOnSuccess() {
        Mockito.`when`<Int?>(rootNodeMock!!.getChildCount()).thenReturn(1)
        Mockito.`when`<AccessibilityNodeInfo?>(rootNodeMock!!.getChild(0)).thenReturn(childNodeMock)
        Mockito.`when`<AccessibilityNodeInfo?>(rootNodeMock!!.getLabeledBy())
            .thenReturn(labelNodeMock)

        enqueueNode(labelNodeMock)
        enqueueNode(childNodeMock)
        enqueueNode(rootNodeMock)

        val axeView = testSubject!!.createAxeViews(rootNodeMock!!)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito.verify<AccessibilityNodeInfo?>(childNodeMock, Mockito.times(1)).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(labelNodeMock, Mockito.times(1)).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.never()).recycle()
    }

    @Test
    @Throws(ViewChangedException::class)
    fun recyclesNodesOnRetry() {
        setupViewChangedScenario(true, true)

        val axeView = testSubject!!.createAxeViews(rootNodeMock!!)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito.verify<AccessibilityNodeInfo?>(childNodeMock, Mockito.times(2)).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(labelNodeMock, Mockito.times(2)).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.never()).recycle()
    }

    @Test
    fun recyclesNodesOnRetryFailure() {
        setupViewChangedScenario(false, true)

        val numRetries = 5

        try {
            testSubject!!.createAxeViews(rootNodeMock!!)
            Assert.fail("Expected createAxeViews to throw exception")
        } catch (e: ViewChangedException) {
            Mockito.verify<AccessibilityNodeInfo?>(childNodeMock, Mockito.times(numRetries + 1))
                .recycle()
            Mockito.verify<AccessibilityNodeInfo?>(labelNodeMock, Mockito.times(numRetries + 1))
                .recycle()
            Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.never()).recycle()
        }
    }

    private fun setupNodeViewCreation(
        builder: NodeViewBuilder?, node: AccessibilityNodeInfo?, view: AxeView?
    ) {
        Mockito.`when`<NodeViewBuilder>(
            nodeViewBuilderFactoryMock!!.createNodeViewBuilder(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(node),
                ArgumentMatchers.any<MutableList<AxeView>>(),
                ArgumentMatchers.any<AxeView?>()
            )
        )
            .thenReturn(builder)
        Mockito.`when`<AxeView?>(builder!!.build()).thenReturn(view)
    }

    private fun setupViewChangedScenario(retryShouldSucceed: Boolean, withLabelNode: Boolean) {
        Mockito.`when`<Int?>(rootNodeMock!!.getChildCount()).thenReturn(1)
        if (retryShouldSucceed) {
            Mockito.`when`<AccessibilityNodeInfo?>(rootNodeMock!!.getChild(0)).thenReturn(null)
                .thenReturn(childNodeMock)
        } else {
            Mockito.`when`<AccessibilityNodeInfo?>(rootNodeMock!!.getChild(0)).thenReturn(null)
        }

        if (withLabelNode) {
            Mockito.`when`<AccessibilityNodeInfo?>(rootNodeMock!!.getLabeledBy())
                .thenReturn(labelNodeMock)
            enqueueNode(labelNodeMock)
        }

        enqueueNode(childNodeMock)
        enqueueNode(rootNodeMock)

        Mockito.reset<AccessibilityNodeInfoQueueBuilder?>(queueBuilderMock)
        Mockito.`when`<Queue<OrderedValue<AccessibilityNodeInfo>>>(
            queueBuilderMock!!.buildPriorityQueue(
                rootNodeMock!!
            )
        )
            .thenAnswer(Answer { rootNodeMock: InvocationOnMock? ->
                LinkedList<OrderedValue<AccessibilityNodeInfo?>?>(
                    queue
                )
            })
    }

    private fun enqueueNode(node: AccessibilityNodeInfo?) {
        val orderedNode = OrderedValue<AccessibilityNodeInfo?>(node, 0L)
        queue!!.add(orderedNode)
    }
}
