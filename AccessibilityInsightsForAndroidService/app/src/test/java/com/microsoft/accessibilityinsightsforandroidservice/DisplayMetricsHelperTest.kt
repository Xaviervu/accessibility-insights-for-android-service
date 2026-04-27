// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class DisplayMetricsHelperTest {
    @Mock
    var resourcesMock: Resources? = null

    @Mock
    var displayMetricsMock: DisplayMetrics? = null

    @Mock
    var contextMock: Context? = null

    @Mock
    var windowManagerMock: WindowManager? = null

    @Mock
    var displayMock: Display? = null

    var resourcesStaticMock: MockedStatic<Resources?>? = null

    @Before
    fun prepare() {
        setupDisplayMocks()
    }

    @After
    fun cleanUp() {
        resourcesStaticMock!!.close()
    }

    @Test
    fun returnsNotNull() {
        Assert.assertNotNull(DisplayMetricsHelper.getRealDisplayMetrics(contextMock!!))
    }

    @Test
    fun returnsExpectedDisplayMetrics() {
        val actualDisplayMetrics = DisplayMetricsHelper.getRealDisplayMetrics(contextMock!!)

        Mockito
            .verify<Display?>(displayMock, VerificationModeFactory.times(1))
            .getRealMetrics(displayMetricsMock)
        Assert.assertEquals(actualDisplayMetrics, displayMetricsMock)
    }

    private fun setupDisplayMocks() {
        resourcesStaticMock = Mockito.mockStatic<Resources?>(Resources::class.java)
        resourcesStaticMock!!
            .`when`<Any?>(Verification { Resources.getSystem() })
            .thenReturn(resourcesMock)
        Mockito
            .`when`<DisplayMetrics?>(resourcesMock!!.getDisplayMetrics())
            .thenReturn(displayMetricsMock)
        Mockito
            .`when`<Any?>(contextMock!!.getSystemService(Context.WINDOW_SERVICE))
            .thenReturn(windowManagerMock)
        Mockito.`when`<Display?>(windowManagerMock!!.getDefaultDisplay()).thenReturn(displayMock)
    }
}
