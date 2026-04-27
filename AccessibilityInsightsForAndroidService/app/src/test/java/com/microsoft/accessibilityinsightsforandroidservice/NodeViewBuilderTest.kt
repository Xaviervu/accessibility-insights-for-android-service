// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeView
import com.deque.axe.android.wrappers.AxeRect
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeRectProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.VoidAnswer1

@RunWith(MockitoJUnitRunner::class)
class NodeViewBuilderTest {
    @Mock
    lateinit var node: AccessibilityNodeInfo

    @Mock
    lateinit var rectProvider: AxeRectProvider
    lateinit var children: MutableList<AxeView>

    private val boundsLeft = 0
    private val boundsRight = 1
    private val boundsTop = 2
    private val boundsBottom = 3
    private val expectedBoundsRect = AxeRect(boundsLeft, boundsRight, boundsTop, boundsBottom)

    var testSubject: NodeViewBuilder? = null

    @Before
    fun prepare() {
        children = ArrayList()
        Mockito
            .`when`(
                rectProvider.createAxeRect(
                    boundsLeft,
                    boundsRight,
                    boundsTop,
                    boundsBottom,
                ),
            ).thenReturn(expectedBoundsRect)
        Mockito.`when`(node.className).thenReturn("class name")
    }

    @Test
    fun nodeViewIsNotNull() {
        testSubject = NodeViewBuilder(node, children, null, rectProvider)
        Assert.assertNotNull(testSubject!!.build())
    }

    @Test
    fun nodeViewHasCorrectBoundingRect() {
        setupBoundingRect()

        testSubject = NodeViewBuilder(node, children, null, rectProvider)
        val axeView = testSubject!!.build()

        val boundingRect = axeView.boundsInScreen

        Assert.assertNotNull(boundingRect)
        Assert.assertEquals(boundingRect, expectedBoundsRect)
    }

    @Test
    fun nodeViewHasChildren() {
        val child1 = Mockito.mock(AxeView::class.java)
        val child2 = Mockito.mock(AxeView::class.java)
        children.add(child1)
        children.add(child2)

        testSubject = NodeViewBuilder(node, children, null, rectProvider)
        val axeView = testSubject?.build()

        val viewChildren = axeView?.children
        Assert.assertNotNull(viewChildren)
        Assert.assertFalse(viewChildren?.isEmpty() == true)
        Assert.assertEquals(viewChildren, children)
    }

    @Test
    fun nodeViewHasLabeledBy() {
        val labeledBy = Mockito.mock<AxeView?>(AxeView::class.java)

        testSubject = NodeViewBuilder(node, children, labeledBy, rectProvider)
        val axeView = testSubject?.build()

        val viewLabeledBy = axeView?.labeledBy
        Assert.assertNotNull(viewLabeledBy)
        Assert.assertSame(viewLabeledBy, labeledBy)
    }

    @Test
    fun nodeViewHasNullClassName() {
        val labeledBy = Mockito.mock<AxeView?>(AxeView::class.java)
        Mockito.`when`<CharSequence?>(node.className).thenReturn(null)

        testSubject = NodeViewBuilder(node, children, labeledBy, rectProvider)
        val axeView = testSubject!!.build()

        Assert.assertNotNull(axeView.className)
    }

    private fun setupBoundingRect() {
        Mockito
            .doAnswer(
                AdditionalAnswers.answerVoid<Any?> { emptyRect: Any? ->
                    val rect = emptyRect as Rect
                    rect.left = boundsLeft
                    rect.top = boundsTop
                    rect.right = boundsRight
                    rect.bottom = boundsBottom
                },
            ).`when`(node)
            .getBoundsInScreen(ArgumentMatchers.any<Rect?>())
    }
}
