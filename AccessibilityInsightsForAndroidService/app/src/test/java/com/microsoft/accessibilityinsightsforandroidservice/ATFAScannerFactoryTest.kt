// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAScannerFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ATFAScannerFactoryTest {
    @Mock
    var contextMock: Context? = null

    @Before
    fun prepare() {
    }

    @Test
    fun atfaScannerExists() {
        Assert.assertNotNull(ATFAScannerFactory.createATFAScanner(contextMock!!))
    }
}
