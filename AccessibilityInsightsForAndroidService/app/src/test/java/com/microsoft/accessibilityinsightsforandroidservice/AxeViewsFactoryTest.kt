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
    lateinit var nodeViewBuilderFactoryMock: NodeViewBuilderFactory

    @Mock
    lateinit var queueBuilderMock: AccessibilityNodeInfoQueueBuilder

    @Mock
    lateinit var rootNodeBuilder: NodeViewBuilder

    @Mock
    lateinit var childNodeBuilder: NodeViewBuilder

    @Mock
    lateinit var labelNodeBuilder: NodeViewBuilder

    @Mock
    lateinit var rootNodeMock: AccessibilityNodeInfo

    @Mock
    lateinit var childNodeMock: AccessibilityNodeInfo

    @Mock
    lateinit var labelNodeMock: AccessibilityNodeInfo

    @Mock
    lateinit  var rootViewMock: AxeView

    @Mock
    lateinit var childViewMock: AxeView

    @Mock
    lateinit var labelViewMock: AxeView

    lateinit var queue: Queue<OrderedValue<AccessibilityNodeInfo>>

    lateinit  var testSubject: AxeViewsFactory

    val nodeClassName: String = "node class name"

    @Before
    fun prepare() {
        setupNodeViewCreation(rootNodeBuilder, rootNodeMock, rootViewMock)
        setupNodeViewCreation(childNodeBuilder, childNodeMock, childViewMock)
        setupNodeViewCreation(labelNodeBuilder, labelNodeMock, labelViewMock)

        queue = LinkedList()
        Mockito
            .`when`(
                queueBuilderMock.buildPriorityQueue(
                    rootNodeMock,
                ),
            ).thenReturn(queue)

        Mockito.`when`<CharSequence?>(childNodeMock.className).thenReturn(nodeClassName)
        Mockito.`when`<CharSequence?>(labelNodeMock.className).thenReturn(nodeClassName)

        testSubject = AxeViewsFactory(nodeViewBuilderFactoryMock, queueBuilderMock)
    }

    @Test
    @Throws(ViewChangedException::class)
    fun axeViewIsNotNull() {
        enqueueNode(rootNodeMock)

        Assert.assertNotNull(testSubject.createAxeViews(rootNodeMock))
    }

    @Test
    @Throws(ViewChangedException::class)
    fun createsAxeViewWithoutChildren() {
        enqueueNode(rootNodeMock)

        val axeView = testSubject.createAxeViews(rootNodeMock)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito
            .verify(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq(rootNodeMock),
                ArgumentMatchers.eq(
                    ArrayList(),
                ),
                ArgumentMatchers.eq<AxeView>(null),
            )
    }

    @Test
    @Throws(ViewChangedException::class)
    fun createsAxeViewWithChildNode() {
        Mockito.`when`(rootNodeMock.childCount).thenReturn(1)
        Mockito.`when`(rootNodeMock.getChild(0)).thenReturn(childNodeMock)

        enqueueNode(childNodeMock)
        enqueueNode(rootNodeMock)

        val axeView = testSubject.createAxeViews(rootNodeMock)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        val children = ArrayList<AxeView>()
        children.add(childViewMock)

        Mockito
            .verify(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq(rootNodeMock),
                ArgumentMatchers.eq(children),
                ArgumentMatchers.eq<AxeView?>(null),
            )
        Mockito
            .verify(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq(childNodeMock),
                ArgumentMatchers.eq<ArrayList<AxeView>>(
                    ArrayList<AxeView>(),
                ),
                ArgumentMatchers.eq<AxeView>(null),
            )
    }

    @Test
    @Throws(ViewChangedException::class)
    fun createsAxeViewWithLabeledByNode() {
        Mockito
            .`when`(rootNodeMock.getLabeledBy())
            .thenReturn(labelNodeMock)

        enqueueNode(labelNodeMock)
        enqueueNode(rootNodeMock)

        val axeView = testSubject.createAxeViews(rootNodeMock)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito
            .verify(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq(rootNodeMock),
                ArgumentMatchers.eq(
                    ArrayList(),
                ),
                ArgumentMatchers.eq(labelViewMock),
            )
        Mockito
            .verify(nodeViewBuilderFactoryMock, Mockito.times(1))
            .createNodeViewBuilder(
                ArgumentMatchers.eq(labelNodeMock),
                ArgumentMatchers.eq(
                    ArrayList(),
                ),
                ArgumentMatchers.eq<AxeView?>(null),
            )
    }

    @Test
    @Throws(ViewChangedException::class)
    fun refreshAndRetryIfViewChanged() {
        setupViewChangedScenario(retryShouldSucceed = true, withLabelNode = false)

        val axeView = testSubject.createAxeViews(rootNodeMock)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito.verify(rootNodeMock, Mockito.times(1)).refresh()
        Mockito
            .verify(queueBuilderMock, Mockito.times(2))
            .buildPriorityQueue(rootNodeMock)
        Mockito.verify(rootNodeMock, Mockito.times(2)).childCount
        Mockito.verify(rootNodeMock, Mockito.times(2)).getChild(0)
    }

    @Test
    fun retriesFiveTimes() {
        setupViewChangedScenario(false, false)

        val numRetries = 5

        try {
            testSubject.createAxeViews(rootNodeMock)
            Assert.fail("Expected createAxeViews to throw exception")
        } catch (_: ViewChangedException) {
            Mockito
                .verify(rootNodeMock, Mockito.times(numRetries))
                .refresh()
            Mockito
                .verify(
                    queueBuilderMock,
                    Mockito.times(numRetries + 1),
                ).buildPriorityQueue(rootNodeMock)
            Mockito
                .verify(rootNodeMock, Mockito.times(numRetries + 1))
                .childCount
            Mockito
                .verify(rootNodeMock, Mockito.times(numRetries + 1))
                .getChild(0)
        }
    }

    @Test
    @Throws(ViewChangedException::class)
    fun recyclesNodesOnSuccess() {
        Mockito.`when`(rootNodeMock.childCount).thenReturn(1)
        Mockito.`when`(rootNodeMock.getChild(0)).thenReturn(childNodeMock)
        Mockito
            .`when`(rootNodeMock.getLabeledBy())
            .thenReturn(labelNodeMock)

        enqueueNode(labelNodeMock)
        enqueueNode(childNodeMock)
        enqueueNode(rootNodeMock)

        val axeView = testSubject.createAxeViews(rootNodeMock)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito.verify(childNodeMock, Mockito.times(1)).recycle()
        Mockito.verify(labelNodeMock, Mockito.times(1)).recycle()
        Mockito.verify(rootNodeMock, Mockito.never()).recycle()
    }

    @Test
    @Throws(ViewChangedException::class)
    fun recyclesNodesOnRetry() {
        setupViewChangedScenario(retryShouldSucceed = true, withLabelNode = true)

        val axeView = testSubject.createAxeViews(rootNodeMock)
        Assert.assertNotNull(axeView)
        Assert.assertEquals(axeView, rootViewMock)

        Mockito.verify(childNodeMock, Mockito.times(2)).recycle()
        Mockito.verify(labelNodeMock, Mockito.times(2)).recycle()
        Mockito.verify(rootNodeMock, Mockito.never()).recycle()
    }

    @Test
    fun recyclesNodesOnRetryFailure() {
        setupViewChangedScenario(retryShouldSucceed = false, withLabelNode = true)

        val numRetries = 5

        try {
            testSubject.createAxeViews(rootNodeMock)
            Assert.fail("Expected createAxeViews to throw exception")
        } catch (_: ViewChangedException) {
            Mockito
                .verify(childNodeMock, Mockito.times(numRetries + 1))
                .recycle()
            Mockito
                .verify(labelNodeMock, Mockito.times(numRetries + 1))
                .recycle()
            Mockito.verify(rootNodeMock, Mockito.never()).recycle()
        }
    }

    private fun setupNodeViewCreation(
        builder: NodeViewBuilder?,
        node: AccessibilityNodeInfo?,
        view: AxeView?,
    ) {
        Mockito
            .`when`(
                nodeViewBuilderFactoryMock.createNodeViewBuilder(
                    ArgumentMatchers.eq<AccessibilityNodeInfo>(node),
                    ArgumentMatchers.any(),
                    ArgumentMatchers.any<AxeView?>(),
                ),
            ).thenReturn(builder)
        Mockito.`when`(builder?.build()).thenReturn(view)
    }

    private fun setupViewChangedScenario(
        retryShouldSucceed: Boolean,
        withLabelNode: Boolean,
    ) {
        Mockito.`when`(rootNodeMock.childCount).thenReturn(1)
        if (retryShouldSucceed) {
            Mockito
                .`when`(rootNodeMock.getChild(0))
                .thenReturn(null)
                .thenReturn(childNodeMock)
        } else {
            Mockito.`when`(rootNodeMock.getChild(0)).thenReturn(null)
        }

        if (withLabelNode) {
            Mockito
                .`when`(rootNodeMock.getLabeledBy())
                .thenReturn(labelNodeMock)
            enqueueNode(labelNodeMock)
        }

        enqueueNode(childNodeMock)
        enqueueNode(rootNodeMock)

        Mockito.reset(queueBuilderMock)
        Mockito
            .`when`(
                queueBuilderMock.buildPriorityQueue(
                    rootNodeMock,
                ),
            ).thenAnswer(
                Answer { _: InvocationOnMock ->
                    LinkedList(
                        queue,
                    )
                },
            )
    }

    private fun enqueueNode(node: AccessibilityNodeInfo) {
        val orderedNode = OrderedValue(node, 0L)
        queue.add(orderedNode)
    }
}
