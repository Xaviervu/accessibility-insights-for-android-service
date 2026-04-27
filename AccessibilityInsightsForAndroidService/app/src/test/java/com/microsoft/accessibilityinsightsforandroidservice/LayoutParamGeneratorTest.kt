// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.WindowManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.MockedConstruction.MockInitializer
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.function.Supplier

@RunWith(MockitoJUnitRunner::class)
class LayoutParamGeneratorTest {
    @Mock
    lateinit var displayMetricsSupplier: Supplier<DisplayMetrics>

    @Mock
    var displayMetrics: DisplayMetrics? = null

    var testSubject: LayoutParamGenerator? = null

    @Before
    fun prepare() {
        testSubject = LayoutParamGenerator(displayMetricsSupplier)
    }

    @Test
    @Throws(Exception::class)
    fun generatesLayoutParams() {
        Mockito.`when`<DisplayMetrics?>(displayMetricsSupplier.get()).thenReturn(displayMetrics)
        Mockito
            .mockConstruction<WindowManager.LayoutParams?>(
                WindowManager.LayoutParams::class.java,
                MockInitializer { mockLayoutParams: WindowManager.LayoutParams?, context: MockedConstruction.Context? ->
                    val args = context!!.arguments()
                    Assert.assertEquals(displayMetrics!!.widthPixels, args.get(0))
                    Assert.assertEquals(displayMetrics!!.heightPixels, args.get(1))
                    Assert.assertEquals(
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        args.get(2),
                    )
                    Assert.assertEquals(
                        (
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        ),
                        args.get(3),
                    )
                    Assert.assertEquals(PixelFormat.TRANSLUCENT, args.get(4))
                },
            ).use { layoutParamsConstructionMock ->
                testSubject!!.get()
            }
    }
}
