// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityNodeInfo
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeDeviceFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.function.Supplier

@RunWith(MockitoJUnitRunner::class)
class AxeDeviceFactoryTest {
    @Mock
    var deviceConfigFactoryMock: DeviceConfigFactory? = null

    @Mock
    var rootNodeMock: AccessibilityNodeInfo? = null
    var deviceName: String = "test-device-name"
    var packageName: String = "test-package-name"
    var serviceVersion: String = "test-service-version"
    var deviceConfig: DeviceConfig? = null
    var displayMetrics: DisplayMetrics? = null

    var testSubject: AxeDeviceFactory? = null

    @Before
    fun prepare() {
        deviceConfig = DeviceConfig(deviceName, packageName, serviceVersion)
        Mockito.`when`<DeviceConfig>(deviceConfigFactoryMock!!.getDeviceConfig(rootNodeMock))
            .thenReturn(deviceConfig)

        displayMetrics = DisplayMetrics()
        displayMetrics!!.density = 1f
        displayMetrics!!.heightPixels = 2
        displayMetrics!!.widthPixels = 3

        testSubject = AxeDeviceFactory(deviceConfigFactoryMock!!, Supplier { displayMetrics!! })
    }

    @Test
    fun axeDeviceIsNotNull() {
        Assert.assertNotNull(testSubject!!.createAxeDevice(rootNodeMock))
    }
}
