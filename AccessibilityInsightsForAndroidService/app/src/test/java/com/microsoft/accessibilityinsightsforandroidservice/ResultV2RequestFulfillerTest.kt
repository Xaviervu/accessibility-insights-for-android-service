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
    lateinit var rootNodeFinder: RootNodeFinder

    @Mock
    lateinit var eventHelper: EventHelper

    @Mock
    lateinit var axeScanner: AxeScanner

    @Mock
    lateinit var atfaScanner: ATFAScanner

    @Mock
    lateinit var screenshotController: ScreenshotController

    @Mock
    lateinit var screenshotMock: Bitmap

    @Mock
    lateinit var sourceNode: AccessibilityNodeInfo

    @Mock
    lateinit var rootNode: AccessibilityNodeInfo

    @Mock
    lateinit var axeResultMock: AxeResult

    @Mock
    lateinit var resultsV2ContainerSerializer: ResultsV2ContainerSerializer

    @Mock
    lateinit var cancellationSignal: CancellationSignal

    val atfaResults: MutableList<AccessibilityHierarchyCheckResult> =
        mutableListOf()
    val scanResultJson: String = "axe scan result"

    lateinit var testSubject: ResultV2RequestFulfiller

    @Before
    fun prepare() {
        setupScreenshotParameter(screenshotMock)
        testSubject =
            ResultV2RequestFulfiller(
                rootNodeFinder,
                eventHelper,
                axeScanner,
                atfaScanner,
                screenshotController,
                resultsV2ContainerSerializer,
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

        testSubject.fulfillRequest(cancellationSignal)

        Mockito
            .verify(screenshotController, Mockito.times(1))
            .getScreenshotWithMediaProjection(ArgumentMatchers.any())
    }

    @Test
    @Throws(Exception::class)
    fun writesSuccessfulResponse() {
        setupSuccessfulRequest()

        Assert.assertEquals(scanResultJson, testSubject.fulfillRequest(cancellationSignal))
    }

    @Test
    @Throws(Exception::class)
    fun recyclesNodes() {
        setupSuccessfulRequest()

        testSubject.fulfillRequest(cancellationSignal)

        Mockito.verify(rootNode, Mockito.times(1)).recycle()
        Mockito.verify(sourceNode, Mockito.times(1)).recycle()
    }

    @Test
    @Throws(Exception::class)
    fun recyclesNodeOnceIfRootEqualsSource() {
        setupSuccessfulRequest()
        Mockito.reset(rootNodeFinder)
        Mockito.reset(axeScanner)
        Mockito
            .`when`<AccessibilityNodeInfo?>(
                rootNodeFinder.getRootNodeFromSource(
                    ArgumentMatchers.any<AccessibilityNodeInfo?>(),
                ),
            ).thenReturn(sourceNode)
        Mockito
            .`when`(
                axeScanner.scanWithAxe(
                    ArgumentMatchers.eq(
                        sourceNode,
                    ),
                    ArgumentMatchers.any(),
                ),
            ).thenReturn(axeResultMock)

        testSubject.fulfillRequest(cancellationSignal)

        Mockito.verifyNoInteractions(rootNode)
        Mockito.verify(sourceNode, Mockito.times(1)).recycle()
    }

    @Test
    @Throws(Exception::class)
    fun throwsExceptionIfNoScreenshot() {
        setupSuccessfulRequest()
        Mockito.reset(screenshotController)
        setupScreenshotParameter(null)

        Assert.assertThrows(
            "Could not acquire screenshot. Has the user granted screen recording permissions?",
            Exception::class.java,
        ) { testSubject.fulfillRequest(cancellationSignal) }
    }

    @Test
    fun throwsExceptionIfNoRootNode() {
        Mockito
            .`when`<AccessibilityNodeInfo?>(rootNodeFinder.getRootNodeFromSource(null))
            .thenReturn(null)

        Assert.assertThrows(
            "Unable to locate root node to scan",
            Exception::class.java,
        ) { testSubject.fulfillRequest(cancellationSignal) }
    }

    @Test
    @Throws(ViewChangedException::class)
    fun throwsExceptionIfScanFailed() {
        Mockito
            .`when`<AccessibilityNodeInfo?>(eventHelper.claimLastSource())
            .thenReturn(sourceNode)
        Mockito
            .`when`<AccessibilityNodeInfo?>(
                rootNodeFinder.getRootNodeFromSource(
                    ArgumentMatchers.any<AccessibilityNodeInfo?>(),
                ),
            ).thenReturn(rootNode)
        Mockito
            .`when`(
                axeScanner.scanWithAxe(
                    ArgumentMatchers.eq(
                        rootNode,
                    ),
                    ArgumentMatchers.any(),
                ),
            ).thenReturn(null)

        Assert.assertThrows(
            "Scanner returned no data",
            Exception::class.java,
        ) { testSubject.fulfillRequest(cancellationSignal) }
    }

    @Test
    @Throws(Exception::class)
    fun doesNotRecycleSourceIfRestoreLastSourceSucceeds() {
        setupSuccessfulRequest()
        Mockito.`when`(eventHelper.restoreLastSource(sourceNode)).thenReturn(true)

        testSubject.fulfillRequest(cancellationSignal)

        Mockito.verify(rootNode, Mockito.times(1)).recycle()
        Mockito.verify(sourceNode, Mockito.never()).recycle()
    }

    @Test
    @Throws(Exception::class)
    fun supportsCancellationBetweenScreenshotAndFirstScan() {
        setupSuccessfulRequest()
        Mockito.reset(screenshotController)
        Mockito
            .doAnswer(
                AdditionalAnswers.answerVoid { bitmapConsumer: Consumer<Bitmap?>? ->
                    simulateCancellation()
                    bitmapConsumer!!.accept(screenshotMock)
                },
            ).`when`(screenshotController)
            .getScreenshotWithMediaProjection(ArgumentMatchers.any())

        Assert.assertThrows(
            OperationCanceledException::class.java,
        ) { testSubject.fulfillRequest(cancellationSignal) }

        Mockito.verifyNoInteractions(axeScanner)
        Mockito.verifyNoInteractions(atfaScanner)
    }

    @Test
    @Throws(Exception::class)
    fun supportsCancellationBetweenScans() {
        setupSuccessfulRequest()
        Mockito.reset<AxeScanner?>(axeScanner)
        Mockito
            .`when`<AxeResult>(
                axeScanner.scanWithAxe(
                    ArgumentMatchers.eq(
                        rootNode,
                    ),
                    ArgumentMatchers.any(),
                ),
            ).thenAnswer(
                Answer { _: InvocationOnMock? ->
                    simulateCancellation()
                    axeResultMock
                },
            )
        Assert.assertThrows(
            OperationCanceledException::class.java,
        ) { testSubject.fulfillRequest(cancellationSignal) }

        Mockito.verifyNoInteractions(atfaScanner)
    }

    private fun simulateCancellation() {
        Mockito
            .doThrow(OperationCanceledException())
            .`when`(cancellationSignal)
            .throwIfCanceled()
    }

    private fun setupSuccessfulRequest() {
        Mockito
            .`when`<AccessibilityNodeInfo?>(eventHelper.claimLastSource())
            .thenReturn(sourceNode)
        Mockito
            .`when`<AccessibilityNodeInfo?>(
                rootNodeFinder.getRootNodeFromSource(
                    ArgumentMatchers.any<AccessibilityNodeInfo?>(),
                ),
            ).thenReturn(rootNode)
        try {
            Mockito
                .`when`<AxeResult>(
                    axeScanner.scanWithAxe(
                        ArgumentMatchers.eq(
                            rootNode,
                        ),
                        ArgumentMatchers.any(),
                    ),
                ).thenReturn(axeResultMock)
        } catch (e: ViewChangedException) {
            Assert.fail(e.message)
        }
        Mockito
            .`when`(
                atfaScanner.scanWithATFA(
                    ArgumentMatchers.eq(sourceNode),
                    ArgumentMatchers.any<BitmapImage>(),
                ),
            ).thenReturn(atfaResults)
        Mockito
            .`when`<String>(
                resultsV2ContainerSerializer.createResultsJson(
                    axeResultMock,
                    atfaResults,
                ),
            ).thenReturn(scanResultJson)
    }

    private fun setupScreenshotParameter(value: Bitmap?) {
        Mockito
            .doAnswer(
                AdditionalAnswers.answerVoid { bitmapConsumer: Consumer<Bitmap?> ->
                    bitmapConsumer.accept(value)
                },
            ).`when`(screenshotController)
            .getScreenshotWithMediaProjection(ArgumentMatchers.any())
    }
}
