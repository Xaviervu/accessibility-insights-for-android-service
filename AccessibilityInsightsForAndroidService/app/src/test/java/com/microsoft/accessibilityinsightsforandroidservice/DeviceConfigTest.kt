// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DeviceConfigTest {
    var deviceName: String = "test-device-name"
    var packageName: String = "test-package-name"
    var serviceVersion: String = "test-service-version"

    var testSubject: DeviceConfig? = null

    @Before
    fun prepare() {
        testSubject = DeviceConfig(deviceName, packageName, serviceVersion)
    }

    @Test
    fun deviceConfigExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun deviceConfigHasExpectedProperties() {
        Assert.assertEquals(deviceName, testSubject!!.deviceName)
        Assert.assertEquals(packageName, testSubject!!.packageName)
        Assert.assertEquals(serviceVersion, testSubject!!.serviceVersion)
    }
}
