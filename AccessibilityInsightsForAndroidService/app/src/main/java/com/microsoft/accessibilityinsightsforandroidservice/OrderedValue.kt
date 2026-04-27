// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Comparable
import kotlin.Int

class OrderedValue<T>(
    @JvmField val value: T,
    @JvmField val order: Long,
) : Comparable<OrderedValue<T>> {
    override fun compareTo(other: OrderedValue<T>): Int = this.order.compareTo(other.order)

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as OrderedValue<*>
        return order == that.order
    }

    override fun hashCode(): Int = Objects.hash(order)
}
