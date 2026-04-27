// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal
import com.microsoft.accessibilityinsightsforandroidservice.Logger.logVerbose
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAScanner
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeScanner
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RequestDispatcherTest {
    @Mock
    var screenshotController: ScreenshotController? = null

    @Mock
    var axeScanner: AxeScanner? = null

    @Mock
    var atfaScanner: ATFAScanner? = null

    @Mock
    var rootNodeFinder: RootNodeFinder? = null

    @Mock
    var eventHelper: EventHelper? = null

    @Mock
    var deviceConfigFactory: DeviceConfigFactory? = null

    @Mock
    var focusVisualizationStateManager: FocusVisualizationStateManager? = null

    @Mock
    var resultsV2ContainerSerializer: ResultsV2ContainerSerializer? = null

    @Mock
    var cancellationSignal: CancellationSignal? = null

    @Mock
    var requestFulfillerMock: RequestFulfiller? = null
    var testSubject: RequestDispatcher? = null

    var loggerStaticMock: MockedStatic<Logger?>? = null

    @Before
    fun prepare() {
        loggerStaticMock = Mockito.mockStatic<Logger?>(Logger::class.java)
        testSubject =
            RequestDispatcher(
                rootNodeFinder!!,
                screenshotController!!,
                eventHelper!!,
                axeScanner!!,
                atfaScanner!!,
                deviceConfigFactory!!,
                focusVisualizationStateManager!!,
                resultsV2ContainerSerializer!!,
            )
    }

    @After
    fun cleanUp() {
        loggerStaticMock!!.close()
    }

    @Throws(Exception::class)
    private fun setupMockRequestFulfiller() {
        testSubject = Mockito.spy<RequestDispatcher>(testSubject)
        Mockito
            .`when`<RequestFulfiller>(testSubject!!.getRequestFulfiller("mock method"))
            .thenReturn(requestFulfillerMock)
        Mockito
            .`when`<String>(requestFulfillerMock!!.fulfillRequest(cancellationSignal!!))
            .thenReturn("mock response")
    }

    @Test
    @Throws(Exception::class)
    fun requestLogsMethod() {
        setupMockRequestFulfiller()

        testSubject!!.request("mock method", cancellationSignal!!)

        loggerStaticMock!!.verify(
            Verification {
                logVerbose(
                    "RequestDispatcher",
                    "Handling request for method mock method",
                )
            },
        )
    }

    @Test
    @Throws(Exception::class)
    fun requestDispatchesToGetRequestFulfiller() {
        // This is testing an implementation detail, but doing so vastly simplifies all the test cases
        // to follow
        setupMockRequestFulfiller()

        val response = testSubject!!.request("mock method", cancellationSignal!!)

        Assert.assertEquals("mock response", response)
    }

    @Test
    fun requestConfigDispatchesToConfigRequestFulfiller() {
        Assert.assertTrue(testSubject!!.getRequestFulfiller("/config") is ConfigRequestFulfiller)
    }

    @Test
    fun requestResultDispatchesToResultV2RequestFulfiller() {
        Assert.assertTrue(testSubject!!.getRequestFulfiller("/result") is ResultV2RequestFulfiller)
    }

    @Test
    fun requestFocusTrackingEnableDispatchesToTabStopsRequestFulfiller() {
        Assert.assertTrue(
            testSubject!!.getRequestFulfiller("/FocusTracking/Enable")
                is TabStopsRequestFulfiller,
        )
    }

    @Test
    fun requestFocusTrackingDisableDispatchesToTabStopsRequestFulfiller() {
        Assert.assertTrue(
            testSubject!!.getRequestFulfiller("/FocusTracking/Disable")
                is TabStopsRequestFulfiller,
        )
    }

    @Test
    fun requestFocusTrackingResetDispatchesToTabStopsRequestFulfiller() {
        Assert.assertTrue(
            testSubject!!.getRequestFulfiller("/FocusTracking/Reset")
                is TabStopsRequestFulfiller,
        )
    }

    @Test
    fun requestForUnknownMethodDispatchesToUnrecognizedRequestFulfiller() {
        Assert.assertTrue(testSubject!!.getRequestFulfiller("/unknown") is UnrecognizedRequestFulfiller)
    }
}
