// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeImageFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AxeImageFactoryTest {
    @Mock
    var byteArrayOutputStreamProviderMock: ByteArrayOutputStreamProvider? = null

    @Mock
    var screenshotMock: Bitmap? = null

    var testSubject: AxeImageFactory? = null

    @Before
    fun prepare() {
        testSubject = AxeImageFactory(byteArrayOutputStreamProviderMock!!)
    }

    @Test
    fun axeImageIsNotNull() {
        Assert.assertNotNull(testSubject!!.createAxeImage(screenshotMock))
    }
}
