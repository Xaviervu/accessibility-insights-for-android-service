// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.Axe
import com.deque.axe.android.AxeContext
import com.deque.axe.android.AxeResult
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeContextFactory
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeRunnerFactory
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeScanner
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AxeScannerTest {
    @Mock
    var screenshotMock: Bitmap? = null

    @Mock
    var accessibilityNodeInfoMock: AccessibilityNodeInfo? = null

    @Mock
    var axeRunnerFactoryMock: AxeRunnerFactory? = null

    @Mock
    var axeContextFactoryMock: AxeContextFactory? = null

    @Mock
    var axeResultMock: AxeResult? = null

    @Mock
    var axeMock: Axe? = null

    @Mock
    var axeContextMock: AxeContext? = null

    var testSubject: AxeScanner? = null

    @Before
    fun prepare() {
        testSubject = AxeScanner(axeRunnerFactoryMock!!, axeContextFactoryMock!!)
    }

    @Test
    @Throws(ViewChangedException::class)
    fun scanWithAxeReturnsCorrectResult() {
        Mockito.`when`<Axe>(axeRunnerFactoryMock!!.createAxeRunner()).thenReturn(axeMock)
        Mockito
            .`when`<AxeContext>(
                axeContextFactoryMock!!.createAxeContext(
                    accessibilityNodeInfoMock!!,
                    screenshotMock!!,
                ),
            ).thenReturn(axeContextMock)
        Mockito.`when`<AxeResult?>(axeMock!!.run(axeContextMock)).thenReturn(axeResultMock)

        Assert.assertEquals(
            testSubject!!.scanWithAxe(accessibilityNodeInfoMock!!, screenshotMock!!),
            axeResultMock,
        )
    }
}
