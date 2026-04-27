// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeRunnerFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AxeRunnerFactoryTest {
    var testSubject: AxeRunnerFactory? = null

    @Before
    fun prepare() {
        testSubject = AxeRunnerFactory()
    }

    @Test
    fun axeRunnerIsNotNull() {
        Assert.assertNotNull(testSubject!!.createAxeRunner())
    }
}
