// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.os.CancellationSignal
import android.view.accessibility.AccessibilityNodeInfo
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class ResultV2RequestFulfiller(
    private val rootNodeFinder: RootNodeFinder,
    private val eventHelper: EventHelper,
    private val axeScanner: AxeScanner,
    private val atfaScanner: ATFAScanner,
    private val screenshotController: ScreenshotController,
    private val resultsV2ContainerSerializer: ResultsV2ContainerSerializer
) : RequestFulfiller {
    @Throws(Exception::class)
    override fun fulfillRequest(cancellationSignal: CancellationSignal): String {
        val successResponse = AtomicReference<String>()
        val errorResponse = AtomicReference<Exception>()
        val doneSignal = CountDownLatch(1)

        screenshotController.getScreenshotWithMediaProjection(
            Consumer { screenshot: Bitmap? ->
                try {
                    cancellationSignal.throwIfCanceled()

                    if (screenshot == null) {
                        throw Exception(
                            "Could not acquire screenshot. Has the user granted screen recording permissions?"
                        )
                    }

                    val source = eventHelper.claimLastSource()
                    val rootNode = rootNodeFinder.getRootNodeFromSource(source)

                    successResponse.set(getScanContent(rootNode!!, screenshot, cancellationSignal))

                    if (rootNode !== source) {
                        rootNode.recycle()
                    }
                    if (source != null && !eventHelper.restoreLastSource(source)) {
                        source.recycle()
                    }
                } catch (e: Exception) {
                    errorResponse.set(e)
                }
                doneSignal.countDown()
            })

        doneSignal.await()
        if (errorResponse.get() != null) {
            throw errorResponse.get()
        }
        return successResponse.get()
    }

    @Throws(ScanException::class, ViewChangedException::class)
    private fun getScanContent(
        rootNode: AccessibilityNodeInfo, screenshot: Bitmap, cancellationSignal: CancellationSignal
    ): String {
        cancellationSignal.throwIfCanceled()
        val axeResult = axeScanner.scanWithAxe(rootNode, screenshot)

        cancellationSignal.throwIfCanceled()
        val atfaResults =
            atfaScanner.scanWithATFA(rootNode, BitmapImage(screenshot))

        return resultsV2ContainerSerializer.createResultsJson(axeResult, atfaResults)
    }
}
