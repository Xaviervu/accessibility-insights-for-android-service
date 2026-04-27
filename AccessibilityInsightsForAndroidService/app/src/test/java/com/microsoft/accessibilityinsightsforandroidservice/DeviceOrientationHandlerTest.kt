// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.function.Consumer

@RunWith(MockitoJUnitRunner::class)
class DeviceOrientationHandlerTest {
    @Mock
    var onChangeMock: Consumer<Int?>? = null
    var initialValue: Int = 1

    var testSubject: DeviceOrientationHandler? = null

    @Before
    fun prepare() {
        testSubject = DeviceOrientationHandler(initialValue)
    }

    @Test
    fun setOrientationDoesNotCallOnChangeListenersIfOrientationDoesNotChange() {
        testSubject!!.subscribe(onChangeMock)
        testSubject!!.setOrientation(initialValue)
        Mockito.verify<Consumer<Int?>?>(onChangeMock, Mockito.times(0)).accept(2)
    }

    @Test
    fun setOrientationCallsOnChangeListenersOnOrientationchange() {
        testSubject!!.subscribe(onChangeMock)
        testSubject!!.setOrientation(2)
        Mockito.verify<Consumer<Int?>?>(onChangeMock, Mockito.times(1)).accept(2)
    }

    @Test
    fun setOrientationSupportsMultipleListeners() {
        testSubject!!.subscribe(onChangeMock)
        testSubject!!.subscribe(onChangeMock)

        testSubject!!.setOrientation(2)

        Mockito.verify<Consumer<Int?>?>(onChangeMock, Mockito.times(2)).accept(2)
    }
}
