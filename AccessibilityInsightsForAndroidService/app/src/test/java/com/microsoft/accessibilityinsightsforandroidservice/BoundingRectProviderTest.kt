// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeRectProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BoundingRectProviderTest {
    var testSubject: AxeRectProvider? = null

    @Before
    fun prepare() {
        testSubject = AxeRectProvider()
    }

    @Test
    fun boundingRectExists() {
        Assert.assertNotNull(testSubject!!.createAxeRect(0, 1, 2, 3))
    }
}
