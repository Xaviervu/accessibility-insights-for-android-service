// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.res.Resources
import android.view.View
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OffsetHelperTest {
    @Mock
    var viewMock: View? = null

    @Mock
    var resourcesMock: Resources? = null

    @Test
    fun getYOffsetReturnsCenterOfElement() {
        Mockito.`when`<Resources?>(viewMock!!.getResources()).thenReturn(resourcesMock)
        Mockito
            .`when`<Int?>(resourcesMock!!.getIdentifier("status_bar_height", "dimen", "android"))
            .thenReturn(1)
        Mockito
            .`when`<Int?>(resourcesMock!!.getDimensionPixelSize(ArgumentMatchers.anyInt()))
            .thenReturn(10)

        Assert.assertEquals(OffsetHelper.getYOffset(viewMock!!).toLong(), 5)
    }
}
