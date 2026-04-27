// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TabStopsRequestFulfillerTest {
    @Mock
    var focusVisualizationStateManager: FocusVisualizationStateManager? = null

    @Mock
    var cancellationSignal: CancellationSignal? = null

    var testSubject: TabStopsRequestFulfiller? = null

    @Test
    fun fulfillRequestSetsTabStopState() {
        testSubject = TabStopsRequestFulfiller(focusVisualizationStateManager!!, true)
        Assert.assertEquals("", testSubject!!.fulfillRequest(cancellationSignal!!))

        Mockito.verify<FocusVisualizationStateManager?>(focusVisualizationStateManager).state = true
    }
}
