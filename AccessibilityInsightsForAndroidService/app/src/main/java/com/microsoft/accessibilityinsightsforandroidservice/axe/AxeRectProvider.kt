// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice.axe

import com.deque.axe.android.wrappers.AxeRect

class AxeRectProvider {
    fun createAxeRect(left: Int, right: Int, top: Int, bottom: Int): AxeRect {
        return AxeRect(left, right, top, bottom)
    }
}
