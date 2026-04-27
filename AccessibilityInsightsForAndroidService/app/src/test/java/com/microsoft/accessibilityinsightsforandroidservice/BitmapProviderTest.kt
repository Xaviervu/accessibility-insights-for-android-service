// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.Bitmap
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BitmapProviderTest {
    @Mock
    var config: Bitmap.Config? = null

    @Mock
    var bitmapMock: Bitmap? = null
    var bitmapStaticMock: MockedStatic<Bitmap?>? = null

    private val width = 1
    private val height = 2

    var testSubject: BitmapProvider? = null

    @Before
    fun prepare() {
        bitmapStaticMock = Mockito.mockStatic<Bitmap?>(Bitmap::class.java)
        bitmapStaticMock!!.`when`<Any?>(Verification {
            Bitmap.createBitmap(
                width,
                height,
                config!!
            )
        }).thenReturn(bitmapMock)
        testSubject = BitmapProvider()
    }

    @After
    fun cleanUp() {
        bitmapStaticMock!!.close()
    }

    @Test
    fun bitmapIsNotNull() {
        val createdBitmap = testSubject!!.createBitmap(width, height, config!!)
        Assert.assertNotNull(createdBitmap)
        Assert.assertEquals(createdBitmap, bitmapMock)

        bitmapStaticMock!!.verify(Verification { Bitmap.createBitmap(width, height, config!!) })
    }
}
