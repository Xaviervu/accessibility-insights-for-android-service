// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.function.ThrowingRunnable
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UnrecognizedRequestFulfillerTest {
    val requestMethod: String = "Test request method"

    @Mock
    var cancellationSignal: CancellationSignal? = null

    var testSubject: UnrecognizedRequestFulfiller? = null

    @Before
    fun prepare() {
        testSubject = UnrecognizedRequestFulfiller(requestMethod)
    }

    @Test
    fun unrecognizedResponseFulfillerExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun fulfillsRequestByThrowingPinnedException() {
        Assert.assertThrows<RuntimeException?>(
            "Unrecognized request: Test request method",
            RuntimeException::class.java,
            ThrowingRunnable { testSubject!!.fulfillRequest(cancellationSignal!!) },
        )
    }
}
