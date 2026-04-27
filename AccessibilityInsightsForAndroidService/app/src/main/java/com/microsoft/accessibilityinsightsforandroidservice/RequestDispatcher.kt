// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal
import com.microsoft.accessibilityinsightsforandroidservice.Logger.logVerbose
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAScanner
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeScanner

class RequestDispatcher(
    private val rootNodeFinder: RootNodeFinder,
    private val screenshotController: ScreenshotController,
    private val eventHelper: EventHelper,
    private val axeScanner: AxeScanner,
    private val atfaScanner: ATFAScanner,
    private val deviceConfigFactory: DeviceConfigFactory,
    private val focusVisualizationStateManager: FocusVisualizationStateManager,
    private val resultsV2ContainerSerializer: ResultsV2ContainerSerializer,
) {
    @Throws(Exception::class)
    fun request(
        method: String,
        cancellationSignal: CancellationSignal,
    ): String? {
        logVerbose(TAG, "Handling request for method $method")
        return getRequestFulfiller(method).fulfillRequest(cancellationSignal)
    }

    fun getRequestFulfiller(method: String): RequestFulfiller {
        when (method) {
            "/config" -> return ConfigRequestFulfiller(
                rootNodeFinder,
                eventHelper,
                deviceConfigFactory,
            )

            "/result" -> return ResultV2RequestFulfiller(
                rootNodeFinder,
                eventHelper,
                axeScanner,
                atfaScanner,
                screenshotController,
                resultsV2ContainerSerializer,
            )

            "/FocusTracking/Enable" -> return TabStopsRequestFulfiller(
                focusVisualizationStateManager,
                true,
            )

            "/FocusTracking/Disable", "/FocusTracking/Reset" -> return TabStopsRequestFulfiller(
                focusVisualizationStateManager,
                false,
            )

            else -> return UnrecognizedRequestFulfiller(method)
        }
    }

    companion object {
        private const val TAG = "RequestDispatcher"
    }
}
