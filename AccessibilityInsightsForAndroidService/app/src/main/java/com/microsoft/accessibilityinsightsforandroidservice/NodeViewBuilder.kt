// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeView
import com.deque.axe.android.wrappers.AxeRect
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeRectProvider

class NodeViewBuilder(
    private val accessibilityNode: AccessibilityNodeInfo,
    private val children: MutableList<AxeView>,
    private val labeledBy: AxeView?,
    boundsRectProvider: AxeRectProvider
) : AxeView.Builder {
    private val boundsRect: AxeRect

    override fun boundsInScreen(): AxeRect {
        return boundsRect
    }

    override fun className(): String {
        val rawClassName = safeToString(accessibilityNode.className)
        return rawClassName ?: "Class Name Not Specified--Inserted by Accessibility Insights"
    }

    override fun contentDescription(): String? {
        return safeToString(accessibilityNode.contentDescription)
    }

    override fun isAccessibilityFocusable(): Boolean {
        return accessibilityNode.isFocusable
    }

    override fun isClickable(): Boolean {
        return accessibilityNode.isClickable
    }

    override fun isEnabled(): Boolean {
        return accessibilityNode.isEnabled
    }

    override fun isImportantForAccessibility(): Boolean {
        return accessibilityNode.isImportantForAccessibility
    }

    override fun labeledBy(): AxeView? {
        return labeledBy
    }

    override fun packageName(): String? {
        return safeToString(accessibilityNode.packageName)
    }

    override fun paneTitle(): String? {
        return null
    }

    override fun text(): String? {
        return safeToString(accessibilityNode.getText())
    }

    override fun viewIdResourceName(): String? {
        return accessibilityNode.viewIdResourceName
    }

    override fun children(): MutableList<AxeView> {
        return children
    }

    override fun value(): String? {
        return null
    }

    override fun hintText(): String? {
        if (Build.VERSION.SDK_INT >= 26) {
            return safeToString(accessibilityNode.hintText)
        }
        return null
    }

    init {
        val rect = Rect()
        accessibilityNode.getBoundsInScreen(rect)
        boundsRect = boundsRectProvider.createAxeRect(rect.left, rect.right, rect.top, rect.bottom)
    }

    private fun safeToString(chars: CharSequence?): String? {
        if (chars == null) {
            return null
        }

        return chars.toString()
    }
}
