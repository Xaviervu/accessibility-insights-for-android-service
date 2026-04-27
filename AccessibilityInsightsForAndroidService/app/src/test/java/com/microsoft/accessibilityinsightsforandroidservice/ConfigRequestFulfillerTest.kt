// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal
import android.view.accessibility.AccessibilityNodeInfo
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ConfigRequestFulfillerTest {
    @Mock
    var rootNodeFinder: RootNodeFinder? = null

    @Mock
    var eventHelper: EventHelper? = null

    @Mock
    var deviceConfigFactory: DeviceConfigFactory? = null

    @Mock
    var sourceNodeMock: AccessibilityNodeInfo? = null

    @Mock
    var rootNodeMock: AccessibilityNodeInfo? = null

    @Mock
    var deviceConfig: DeviceConfig? = null

    @Mock
    var cancellationSignal: CancellationSignal? = null

    var configJson: String = "test config"

    var testSubject: ConfigRequestFulfiller? = null

    @Before
    fun prepare() {
        testSubject = ConfigRequestFulfiller(rootNodeFinder!!, eventHelper!!, deviceConfigFactory!!)
    }

    @Test
    fun configRequestFulfillerExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun writesCorrectResponse() {
        setupSuccessfulRequest()

        Assert.assertEquals(configJson, testSubject!!.fulfillRequest(cancellationSignal!!))
    }

    @Test
    fun recyclesNodes() {
        setupSuccessfulRequest()

        testSubject!!.fulfillRequest(cancellationSignal!!)

        Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.times(1)).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(sourceNodeMock, Mockito.times(1)).recycle()
    }

    @Test
    fun recyclesNodeOnceIfRootEqualsSource() {
        setupSuccessfulRequest()
        Mockito.reset<RootNodeFinder?>(rootNodeFinder)
        Mockito.reset<DeviceConfigFactory?>(deviceConfigFactory)
        Mockito
            .`when`<AccessibilityNodeInfo?>(
                rootNodeFinder!!.getRootNodeFromSource(
                    ArgumentMatchers.any<AccessibilityNodeInfo?>(),
                ),
            ).thenReturn(sourceNodeMock)
        Mockito
            .`when`<DeviceConfig>(deviceConfigFactory!!.getDeviceConfig(sourceNodeMock))
            .thenReturn(deviceConfig)

        testSubject!!.fulfillRequest(cancellationSignal!!)

        Mockito.verifyNoInteractions(rootNodeMock)
        Mockito.verify<AccessibilityNodeInfo?>(sourceNodeMock, Mockito.times(1)).recycle()
    }

    @Test
    fun doesNotRecycleSourceIfRestoreLastSourceSucceeds() {
        setupSuccessfulRequest()
        Mockito.`when`<Boolean?>(eventHelper!!.restoreLastSource(sourceNodeMock)).thenReturn(true)

        testSubject!!.fulfillRequest(cancellationSignal!!)
        Mockito.verify<AccessibilityNodeInfo?>(rootNodeMock, Mockito.times(1)).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(sourceNodeMock, Mockito.never()).recycle()
    }

    private fun setupSuccessfulRequest() {
        Mockito
            .`when`<AccessibilityNodeInfo?>(eventHelper!!.claimLastSource())
            .thenReturn(sourceNodeMock)
        Mockito
            .`when`<AccessibilityNodeInfo?>(rootNodeFinder!!.getRootNodeFromSource(sourceNodeMock))
            .thenReturn(rootNodeMock)
        Mockito
            .`when`<DeviceConfig>(deviceConfigFactory!!.getDeviceConfig(rootNodeMock))
            .thenReturn(deviceConfig)
        Mockito.`when`<String?>(deviceConfig!!.toJson()).thenReturn(configJson)
    }
}
