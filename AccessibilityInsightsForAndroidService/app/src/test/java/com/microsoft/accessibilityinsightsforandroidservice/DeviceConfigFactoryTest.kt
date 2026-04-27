// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DeviceConfigFactoryTest {
    val samplePackageName: CharSequence = "test-package-name"
    val packageNameUnavailable: String = "No application detected"
    val sampleBuildModel: String = "test-build-model"

    @Mock
    var mockRootNode: AccessibilityNodeInfo? = null

    var testSubject: DeviceConfigFactory? = null

    @Before
    fun prepare() {
        testSubject = DeviceConfigFactory(sampleBuildModel)
    }

    @Test
    fun deviceConfigFactoryExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun getDeviceConfigReturnsNonNullDeviceConfig() {
        Assert.assertNotNull(testSubject!!.getDeviceConfig(mockRootNode))
    }

    @Test
    fun deviceConfigFactoryPropertiesExist() {
        val deviceConfig = testSubject!!.getDeviceConfig(mockRootNode)

        Assert.assertNotNull(deviceConfig.deviceName)
        Assert.assertEquals(sampleBuildModel, deviceConfig.deviceName)
        Assert.assertNotNull(deviceConfig.packageName)
        Assert.assertNotNull(deviceConfig.serviceVersion)
    }

    @Test
    fun deviceConfigFactoryGetsProperPackageName() {
        Mockito.`when`<CharSequence?>(mockRootNode!!.getPackageName()).thenReturn(samplePackageName)

        Assert.assertEquals(samplePackageName, getActualPackageName(mockRootNode))
    }

    @Test
    fun deviceConfigFactoryGetsNoPackageNameWhenPackageNameIsNull() {
        Mockito.`when`<CharSequence?>(mockRootNode!!.getPackageName()).thenReturn(null)

        Assert.assertEquals(packageNameUnavailable, getActualPackageName(mockRootNode))
    }

    @Test
    fun deviceConfigFactoryGetsNoPackageNameWhenRootNodeIsNull() {
        Assert.assertEquals(packageNameUnavailable, getActualPackageName(null))
    }

    private fun getActualPackageName(rootNode: AccessibilityNodeInfo?): String? = testSubject!!.getDeviceConfig(rootNode).packageName
}
