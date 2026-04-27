// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ByteArrayOutputStreamProviderTest {
    var testSubject: ByteArrayOutputStreamProvider? = null

    @Before
    fun prepare() {
        testSubject = ByteArrayOutputStreamProvider()
    }

    @Test
    fun byteArrayOutputStreamExists() {
        Assert.assertNotNull(testSubject!!.get())
    }
}
