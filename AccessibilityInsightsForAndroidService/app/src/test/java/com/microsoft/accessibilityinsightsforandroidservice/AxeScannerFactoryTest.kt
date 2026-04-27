// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.util.DisplayMetrics
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeScannerFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.function.Supplier

@RunWith(MockitoJUnitRunner::class)
class AxeScannerFactoryTest {
    @Mock
    var deviceConfigFactoryMock: DeviceConfigFactory? = null

    @Mock
    var displayMetricsMock: DisplayMetrics? = null

    @Before
    fun prepare() {
    }

    @Test
    fun axeScannerExists() {
        Assert.assertNotNull(
            AxeScannerFactory.createAxeScanner(
                deviceConfigFactoryMock!!,
                Supplier { displayMetricsMock!! },
            ),
        )
    }
}
