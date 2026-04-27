// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.function.Consumer

@RunWith(MockitoJUnitRunner::class)
class FocusVisualizationStateManagerTest {
    @Mock
    var onChangeMock: Consumer<Boolean?>? = null

    var testSubject: FocusVisualizationStateManager? = null

    @Before
    fun prepare() {
        testSubject = FocusVisualizationStateManager()
    }

    @Test
    fun exists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun getStateReturnsFalseByDefault() {
        Assert.assertFalse(testSubject!!.state)
    }

    @Test
    fun getStateReturnsUpdatedState() {
        testSubject!!.state = true
        Assert.assertTrue(testSubject!!.state)
    }

    @Test
    fun setStateDoesNotCallOnChangeListenersIfStateDoesNotChange() {
        testSubject!!.subscribe(onChangeMock)
        testSubject!!.state = false
        Mockito.verify<Consumer<Boolean?>?>(onChangeMock, Mockito.times(0)).accept(false)
    }

    @Test
    fun setStateCallsOnChangeListenersOnStateChange() {
        testSubject!!.subscribe(onChangeMock)
        testSubject!!.state = true
        Assert.assertTrue(testSubject!!.state)
        Mockito.verify<Consumer<Boolean?>?>(onChangeMock, Mockito.times(1)).accept(true)
    }
}
