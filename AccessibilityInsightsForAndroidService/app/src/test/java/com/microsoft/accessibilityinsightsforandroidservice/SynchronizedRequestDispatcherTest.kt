// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal
import android.os.OperationCanceledException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.function.ThrowingRunnable
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.MockedConstruction.MockInitializer
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@RunWith(MockitoJUnitRunner::class)
class SynchronizedRequestDispatcherTest {
    @Mock
    var underlyingDispatcher: RequestDispatcher? = null

    var cancellationSignal: CancellationSignal? = null
    var testSubject: SynchronizedRequestDispatcher? = null

    var cancellationSignalConstructionMock: MockedConstruction<CancellationSignal?>? = null

    @Before
    @Throws(Exception::class)
    fun prepare() {
        cancellationSignal = Mockito.mock<CancellationSignal>(CancellationSignal::class.java)
        setupBasicCancellationSignal(cancellationSignal!!)

        cancellationSignalConstructionMock = setupBasicCancellationSignalsOnThisThread()

        testSubject = SynchronizedRequestDispatcher()
    }

    @After
    @Throws(Exception::class)
    fun cleanUp() {
        cancellationSignalConstructionMock!!.close()
    }

    @Test
    @Throws(Exception::class)
    fun teardownWaitsForOutstandingRequests() {
        testSubject!!.setup(underlyingDispatcher!!)

        val delayedRequest = setupCancellableDelayedRequest()
        val request1Thread =
            startOnNewThread(
                ThrowingRunnable {
                    testSubject!!.request(
                        "cancellable delayed method",
                        cancellationSignal!!
                    )
                })
        request1Thread.start()

        delayedRequest.waitForDelayedState()

        val teardownThread = startOnNewThread(ThrowingRunnable { testSubject!!.teardown() })
        teardownThread.start()

        // teardown shouldn't run until request1 is allowed to finish
        teardownThread.join(THREAD_DELAY_ALLOWANCE_MS.toLong())
        Assert.assertTrue(teardownThread.isAlive())

        delayedRequest.allowToFinishOrBeCancelled()
        request1Thread.join(THREAD_JOIN_TIMEOUT_MS.toLong())
        Assert.assertFalse(request1Thread.isAlive())

        teardownThread.join(THREAD_JOIN_TIMEOUT_MS.toLong())
        Assert.assertFalse(teardownThread.isAlive())
    }

    @Test
    fun multipleSetupThrowsException() {
        testSubject!!.setup(underlyingDispatcher!!)
        Assert.assertThrows<Exception?>(
            "RequestDispatcher cannot be set up twice",
            Exception::class.java,
            ThrowingRunnable { testSubject!!.setup(underlyingDispatcher!!) })
    }

    @Test
    fun teardownWithoutSetupIsNoop() {
        testSubject!!.teardown() // shouldn't throw
    }

    @Test
    fun teardownIsIdempotent() {
        testSubject!!.setup(underlyingDispatcher!!)
        testSubject!!.teardown()
        testSubject!!.teardown() // shouldn't throw
    }

    @Test
    @Throws(Exception::class)
    fun teardownCancelsOngoingRequests() {
        testSubject!!.setup(underlyingDispatcher!!)
        val delayedRequest = setupCancellableDelayedRequest()
        val requestThread =
            Thread(
                Runnable {
                    Assert.assertThrows<OperationCanceledException?>(
                        OperationCanceledException::class.java,
                        ThrowingRunnable { testSubject!!.request("method", cancellationSignal!!) })
                })
        requestThread.start()

        testSubject!!.teardown()
        delayedRequest.allowToFinishOrBeCancelled()
        requestThread.join(THREAD_JOIN_TIMEOUT_MS.toLong())
        Assert.assertFalse(requestThread.isAlive())
    }

    @Test
    fun requestWithoutSetupThrowsException() {
        Assert.assertThrows<Exception?>(
            "Service is not running",
            Exception::class.java,
            ThrowingRunnable { testSubject!!.request("any method", cancellationSignal!!) })
    }

    @Test
    @Throws(Exception::class)
    fun requestWaitsForOutstandingRequestToFinish() {
        testSubject!!.setup(underlyingDispatcher!!)

        val delayedRequest = setupCancellableDelayedRequest()
        val request1Thread =
            startOnNewThread(
                ThrowingRunnable {
                    testSubject!!.request(
                        "cancellable delayed method",
                        cancellationSignal!!
                    )
                })
        request1Thread.start()

        delayedRequest.waitForDelayedState()

        setupImmediatelySuccessfulRequest()
        val request2Thread =
            startOnNewThread(
                ThrowingRunnable {
                    testSubject!!.request(
                        "immediately successful method",
                        cancellationSignal!!
                    )
                })
        request2Thread.start()

        // request2 shouldn't run until request1 is allowed to finish
        request2Thread.join(THREAD_DELAY_ALLOWANCE_MS.toLong())
        Assert.assertTrue(request2Thread.isAlive())

        delayedRequest.allowToFinishOrBeCancelled()
        request1Thread.join(THREAD_JOIN_TIMEOUT_MS.toLong())
        Assert.assertFalse(request1Thread.isAlive())

        request2Thread.join(THREAD_JOIN_TIMEOUT_MS.toLong())
        Assert.assertFalse(request2Thread.isAlive())
    }

    @Test
    @Throws(Exception::class)
    fun requestPropagatesResponseFromUnderlyingDispatcher() {
        testSubject!!.setup(underlyingDispatcher!!)
        Mockito.`when`<String?>(
            underlyingDispatcher!!.request(
                ArgumentMatchers.eq<String?>("method"),
                ArgumentMatchers.any<CancellationSignal>()
            )
        )
            .thenReturn("response from underlying dispatcher")

        val response = testSubject!!.request("method", cancellationSignal!!)

        Assert.assertEquals("response from underlying dispatcher", response)
    }

    @Test
    @Throws(Exception::class)
    fun requestPropagatesExceptionFromUnderlyingDispatcher() {
        testSubject!!.setup(underlyingDispatcher!!)
        Mockito.`when`<String?>(
            underlyingDispatcher!!.request(
                ArgumentMatchers.eq<String?>("method"),
                ArgumentMatchers.any<CancellationSignal>()
            )
        )
            .thenThrow(RuntimeException("exception from underlying dispatcher"))

        Assert.assertThrows<RuntimeException?>(
            "exception from underlying dispatcher",
            RuntimeException::class.java,
            ThrowingRunnable { testSubject!!.request("method", cancellationSignal!!) })
    }

    @Test
    @Throws(Exception::class)
    fun requestPropagatesCancellationToUnderlyingDispatcher() {
        testSubject!!.setup(underlyingDispatcher!!)
        val delayedRequest = setupCancellableDelayedRequest()
        val requestThread =
            startOnNewThread(
                ThrowingRunnable {
                    Assert.assertThrows<OperationCanceledException?>(
                        OperationCanceledException::class.java,
                        ThrowingRunnable { testSubject!!.request("method", cancellationSignal!!) })
                })
        requestThread.start()

        cancellationSignal!!.cancel()
        delayedRequest.allowToFinishOrBeCancelled()
        requestThread.join(THREAD_JOIN_TIMEOUT_MS.toLong())
        Assert.assertFalse(requestThread.isAlive())
    }

    private fun setupBasicCancellationSignalsOnThisThread(): MockedConstruction<CancellationSignal?> {
        return Mockito.mockConstruction<CancellationSignal?>(
            CancellationSignal::class.java,
            MockInitializer { mockSignal: CancellationSignal?, context: MockedConstruction.Context? ->
                setupBasicCancellationSignal(mockSignal!!)
            })
    }

    private fun startOnNewThread(callback: ThrowingRunnable): Thread {
        return Thread(
            Runnable {
                try {
                    setupBasicCancellationSignalsOnThisThread().use { cancellationSignalConstructionMock ->
                        callback.run()
                    }
                } catch (e: Throwable) {
                    Assert.fail(
                        ("unexpected exception from async request:\n\n"
                                + e.message
                                + "\n\n"
                                + e.getStackTrace().contentToString())
                    )
                }
            })
    }

    var delayedRequestPollIntervalMillis: Int = 100

    private enum class DelayedRequestState {
        NotStarted,
        Delaying,
        Finishing,
        Finished
    }

    private inner class DelayedRequest {
        var state: DelayedRequestState = DelayedRequestState.NotStarted

        @Synchronized
        @Throws(InterruptedException::class)
        fun request(cancellationSignal: CancellationSignal): String {
            state = DelayedRequestState.Delaying
            while (state == DelayedRequestState.Delaying) {
                (this as Object).wait(delayedRequestPollIntervalMillis.toLong())
            }
            cancellationSignal.throwIfCanceled()
            state = DelayedRequestState.Finished

            return "cancellable delayed method response"
        }

        @Synchronized
        fun allowToFinishOrBeCancelled() {
            state = DelayedRequestState.Finishing
        }

        @Synchronized
        @Throws(InterruptedException::class)
        fun waitForDelayedState() {
            while (state == DelayedRequestState.NotStarted) {
                (this as Object).wait(delayedRequestPollIntervalMillis.toLong())
            }
            if (state != DelayedRequestState.Delaying) {
                throw RuntimeException("State progressed further than expected")
            }
        }
    }

    // Returns a callback which finishes the delayed request
    @Throws(Exception::class)
    private fun setupCancellableDelayedRequest(): DelayedRequest {
        val delayedRequest = DelayedRequest()

        Mockito.`when`<String?>(
            underlyingDispatcher!!.request(
                ArgumentMatchers.eq<String?>("cancellable delayed method"),
                ArgumentMatchers.any<CancellationSignal>()
            )
        )
            .thenAnswer(
                Answer { invocation: InvocationOnMock? ->
                    val signal = invocation!!.getArgument<CancellationSignal>(1)
                    delayedRequest.request(signal)
                })

        return delayedRequest
    }

    @Throws(Exception::class)
    private fun setupImmediatelySuccessfulRequest() {
        Mockito.`when`<String?>(
            underlyingDispatcher!!.request(
                ArgumentMatchers.eq<String?>("immediately successful method"),
                ArgumentMatchers.any<CancellationSignal>()
            )
        )
            .thenReturn("immediately successful method response")
    }

    private fun setupBasicCancellationSignal(mockSignal: CancellationSignal): CancellationSignal {
        val isCancelled = AtomicBoolean(false)
        val onCancelListener =
            AtomicReference<CancellationSignal.OnCancelListener?>(null)

        Mockito.doAnswer(
            Answer { invocation: InvocationOnMock? ->
                synchronized(mockSignal) {
                    isCancelled.set(true)
                    val listener = onCancelListener.getAndSet(null)
                    if (listener != null) {
                        listener.onCancel()
                    }
                }
                null
            })
            .`when`<CancellationSignal?>(mockSignal)
            .cancel()

        // lenient() prevents Mockito from throwing an UnnecessaryStubbingException - Mockito's default
        // strict() context complains that this is unused because it is only ever used on secondary
        // test threads, not the original @Test thread.
        Mockito.lenient()
            .doAnswer(
                Answer { invocation: InvocationOnMock? ->
                    synchronized(mockSignal) {
                        if (isCancelled.get()) {
                            throw OperationCanceledException()
                        }
                    }
                    null
                })
            .`when`<CancellationSignal?>(mockSignal)
            .throwIfCanceled()

        Mockito.doAnswer(
            Answer { invocation: InvocationOnMock? ->
                val listener = invocation!!.getArgument<CancellationSignal.OnCancelListener>(0)
                synchronized(mockSignal) {
                    if (isCancelled.get()) {
                        listener.onCancel()
                    } else {
                        onCancelListener.set(listener)
                    }
                }
                null
            })
            .`when`<CancellationSignal?>(mockSignal)
            .setOnCancelListener(ArgumentMatchers.any<CancellationSignal.OnCancelListener?>())

        return mockSignal
    }

    companion object {
        private const val THREAD_DELAY_ALLOWANCE_MS = 500
        private const val THREAD_JOIN_TIMEOUT_MS = 5000
    }
}
