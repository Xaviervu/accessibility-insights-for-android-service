// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.Handler
import android.os.Looper
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.MockedConstruction.MockInitializer
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer

@RunWith(MockitoJUnitRunner::class)
class UIThreadRunnerTest {
    @Mock
    var looperMock: Looper? = null

    @Mock
    var runnableMock: Runnable? = null

    var testSubject: UIThreadRunner? = null

    @Test
    @Throws(Exception::class)
    fun createsNewHandlerUsingMainLooper() {
        Mockito.mockStatic<Looper?>(Looper::class.java).use { looperStaticMock ->
            looperStaticMock.`when`<Any?>(Verification { Looper.getMainLooper() })
                .thenReturn(looperMock)
            Mockito.mockConstruction<Handler?>(
                Handler::class.java,
                MockInitializer { handlerMock: Handler?, context: MockedConstruction.Context? ->
                    Mockito.doAnswer(
                        Answer { invocation: InvocationOnMock? ->
                            val runnable = invocation!!.getArgument<Runnable>(0)
                            runnable.run()
                            null
                        })
                        .`when`<Handler?>(handlerMock)
                        .post(ArgumentMatchers.any<Runnable?>())
                }).use { handlerConstructionMock ->
                testSubject = UIThreadRunner()
                testSubject!!.run(runnableMock!!)
                Mockito.verify<Runnable?>(runnableMock).run()
            }
        }
    }
}
