// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.view.accessibility.AccessibilityNodeInfo
import com.deque.axe.android.AxeResult
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAScanner
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeScanner
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.function.ThrowingRunnable
import org.junit.runner.RunWith
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer
import org.mockito.stubbing.VoidAnswer1
import java.util.function.Consumer

@RunWith(MockitoJUnitRunner::class)
class ResultV2RequestFulfillerTest {
    @Mock
    var rootNodeFinder: RootNodeFinder? = null

    @Mock
    var eventHelper: EventHelper? = null

    @Mock
    var axeScanner: AxeScanner? = null

    @Mock
    var atfaScanner: ATFAScanner? = null

    @Mock
    var screenshotController: ScreenshotController? = null

    @Mock
    var screenshotMock: Bitmap? = null

    @Mock
    var sourceNode: AccessibilityNodeInfo? = null

    @Mock
    var rootNode: AccessibilityNodeInfo? = null

    @Mock
    var axeResultMock: AxeResult? = null

    @Mock
    var resultsV2ContainerSerializer: ResultsV2ContainerSerializer? = null

    @Mock
    var cancellationSignal: CancellationSignal? = null

    val atfaResults: MutableList<AccessibilityHierarchyCheckResult?> =
        mutableListOf<AccessibilityHierarchyCheckResult?>()
    val scanResultJson: String = "axe scan result"

    var testSubject: ResultV2RequestFulfiller? = null

    @Before
    fun prepare() {
        setupScreenshotParameter(screenshotMock)
        testSubject =
            ResultV2RequestFulfiller(
                rootNodeFinder!!,
                eventHelper!!,
                axeScanner!!,
                atfaScanner!!,
                screenshotController!!,
                resultsV2ContainerSerializer!!
            )
    }

    @Test
    fun resultRequestFulfillerExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    @Throws(Exception::class)
    fun callsGetScreenshotWithMediaProjection() {
        setupSuccessfulRequest()

        testSubject!!.fulfillRequest(cancellationSignal!!)

        Mockito.verify<ScreenshotController?>(screenshotController, Mockito.times(1))
            .getScreenshotWithMediaProjection(ArgumentMatchers.any<Consumer<Bitmap?>>())
    }

    @Test
    @Throws(Exception::class)
    fun writesSuccessfulResponse() {
        setupSuccessfulRequest()

        Assert.assertEquals(scanResultJson, testSubject!!.fulfillRequest(cancellationSignal!!))
    }

    @Test
    @Throws(Exception::class)
    fun recyclesNodes() {
        setupSuccessfulRequest()

        testSubject!!.fulfillRequest(cancellationSignal!!)

        Mockito.verify<AccessibilityNodeInfo?>(rootNode, Mockito.times(1)).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(sourceNode, Mockito.times(1)).recycle()
    }

    @Test
    @Throws(Exception::class)
    fun recyclesNodeOnceIfRootEqualsSource() {
        setupSuccessfulRequest()
        Mockito.reset<RootNodeFinder?>(rootNodeFinder)
        Mockito.reset<AxeScanner?>(axeScanner)
        Mockito.`when`<AccessibilityNodeInfo?>(
            rootNodeFinder!!.getRootNodeFromSource(
                ArgumentMatchers.any<AccessibilityNodeInfo?>()
            )
        ).thenReturn(sourceNode)
        Mockito.`when`<AxeResult>(
            axeScanner!!.scanWithAxe(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(
                    sourceNode
                ), ArgumentMatchers.any<Bitmap>()
            )
        ).thenReturn(axeResultMock)

        testSubject!!.fulfillRequest(cancellationSignal!!)

        Mockito.verifyNoInteractions(rootNode)
        Mockito.verify<AccessibilityNodeInfo?>(sourceNode, Mockito.times(1)).recycle()
    }

    @Test
    @Throws(Exception::class)
    fun throwsExceptionIfNoScreenshot() {
        setupSuccessfulRequest()
        Mockito.reset<ScreenshotController?>(screenshotController)
        setupScreenshotParameter(null)

        Assert.assertThrows<Exception?>(
            "Could not acquire screenshot. Has the user granted screen recording permissions?",
            Exception::class.java,
            ThrowingRunnable { testSubject!!.fulfillRequest(cancellationSignal!!) })
    }

    @Test
    fun throwsExceptionIfNoRootNode() {
        Mockito.`when`<AccessibilityNodeInfo?>(rootNodeFinder!!.getRootNodeFromSource(null))
            .thenReturn(null)

        Assert.assertThrows<Exception?>(
            "Unable to locate root node to scan",
            Exception::class.java,
            ThrowingRunnable { testSubject!!.fulfillRequest(cancellationSignal!!) })
    }

    @Test
    @Throws(ViewChangedException::class)
    fun throwsExceptionIfScanFailed() {
        Mockito.`when`<AccessibilityNodeInfo?>(eventHelper!!.claimLastSource())
            .thenReturn(sourceNode)
        Mockito.`when`<AccessibilityNodeInfo?>(
            rootNodeFinder!!.getRootNodeFromSource(
                ArgumentMatchers.any<AccessibilityNodeInfo?>()
            )
        ).thenReturn(rootNode)
        Mockito.`when`<AxeResult>(
            axeScanner!!.scanWithAxe(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(
                    rootNode
                ), ArgumentMatchers.any<Bitmap>()
            )
        ).thenReturn(null)

        Assert.assertThrows<Exception?>(
            "Scanner returned no data",
            Exception::class.java,
            ThrowingRunnable { testSubject!!.fulfillRequest(cancellationSignal!!) })
    }

    @Test
    @Throws(Exception::class)
    fun doesNotRecycleSourceIfRestoreLastSourceSucceeds() {
        setupSuccessfulRequest()
        Mockito.`when`<Boolean?>(eventHelper!!.restoreLastSource(sourceNode)).thenReturn(true)

        testSubject!!.fulfillRequest(cancellationSignal!!)

        Mockito.verify<AccessibilityNodeInfo?>(rootNode, Mockito.times(1)).recycle()
        Mockito.verify<AccessibilityNodeInfo?>(sourceNode, Mockito.never()).recycle()
    }

    @Test
    @Throws(Exception::class)
    fun supportsCancellationBetweenScreenshotAndFirstScan() {
        setupSuccessfulRequest()
        Mockito.reset<ScreenshotController?>(screenshotController)
        Mockito.doAnswer(
            AdditionalAnswers.answerVoid<Consumer<Bitmap?>?>(
                VoidAnswer1 { bitmapConsumer: Consumer<Bitmap?>? ->
                    simulateCancellation()
                    bitmapConsumer!!.accept(screenshotMock)
                })
        )
            .`when`<ScreenshotController?>(screenshotController)
            .getScreenshotWithMediaProjection(ArgumentMatchers.any<Consumer<Bitmap?>>())

        Assert.assertThrows<OperationCanceledException?>(
            OperationCanceledException::class.java,
            ThrowingRunnable { testSubject!!.fulfillRequest(cancellationSignal!!) })

        Mockito.verifyNoInteractions(axeScanner)
        Mockito.verifyNoInteractions(atfaScanner)
    }

    @Test
    @Throws(Exception::class)
    fun supportsCancellationBetweenScans() {
        setupSuccessfulRequest()
        Mockito.reset<AxeScanner?>(axeScanner)
        Mockito.`when`<AxeResult>(
            axeScanner!!.scanWithAxe(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(
                    rootNode
                ), ArgumentMatchers.any<Bitmap>()
            )
        )
            .thenAnswer(
                Answer { invocation: InvocationOnMock? ->
                    simulateCancellation()
                    axeResultMock
                })
        Assert.assertThrows<OperationCanceledException?>(
            OperationCanceledException::class.java,
            ThrowingRunnable { testSubject!!.fulfillRequest(cancellationSignal!!) })

        Mockito.verifyNoInteractions(atfaScanner)
    }

    private fun simulateCancellation() {
        Mockito.doThrow(OperationCanceledException())
            .`when`<CancellationSignal?>(cancellationSignal).throwIfCanceled()
    }

    private fun setupSuccessfulRequest() {
        Mockito.`when`<AccessibilityNodeInfo?>(eventHelper!!.claimLastSource())
            .thenReturn(sourceNode)
        Mockito.`when`<AccessibilityNodeInfo?>(
            rootNodeFinder!!.getRootNodeFromSource(
                ArgumentMatchers.any<AccessibilityNodeInfo?>()
            )
        ).thenReturn(rootNode)
        try {
            Mockito.`when`<AxeResult>(
                axeScanner!!.scanWithAxe(
                    ArgumentMatchers.eq<AccessibilityNodeInfo?>(
                        rootNode
                    ), ArgumentMatchers.any<Bitmap>()
                )
            ).thenReturn(axeResultMock)
        } catch (e: ViewChangedException) {
            Assert.fail(e.message)
        }
        Mockito.`when`<MutableList<AccessibilityHierarchyCheckResult?>>(
            atfaScanner!!.scanWithATFA(
                ArgumentMatchers.eq<AccessibilityNodeInfo?>(sourceNode),
                ArgumentMatchers.any<BitmapImage?>()
            )
        ).thenReturn(atfaResults)
        Mockito.`when`<String>(
            resultsV2ContainerSerializer!!.createResultsJson(
                axeResultMock,
                atfaResults
            )
        )
            .thenReturn(scanResultJson)
    }

    private fun setupScreenshotParameter(value: Bitmap?) {
        Mockito.doAnswer(
            AdditionalAnswers.answerVoid<Consumer<Bitmap?>?>(
                VoidAnswer1 { bitmapConsumer: Consumer<Bitmap?>? ->
                    bitmapConsumer!!.accept(value)
                })
        )
            .`when`<ScreenshotController?>(screenshotController)
            .getScreenshotWithMediaProjection(ArgumentMatchers.any<Consumer<Bitmap?>>())
    }
}
