// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.Parameters
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage
import com.google.common.collect.ImmutableSet
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAScanner
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ATFAScannerTest {
    @Mock
    var bitmapMock: Bitmap? = null

    @Mock
    var accessibilityNodeInfoMock: AccessibilityNodeInfo? = null

    @Mock
    var contextMock: Context? = null

    @Mock
    var checkMock: AccessibilityHierarchyCheck? = null

    @Mock
    var hierarchyMock: AccessibilityHierarchyAndroid? = null

    @Mock
    var builderMock: AccessibilityHierarchyAndroid.BuilderAndroid? = null

    var accessibilityCheckPresetStaticMock: MockedStatic<AccessibilityCheckPreset?>? = null
    var accessibilityHierarchyAndroidStaticMock: MockedStatic<AccessibilityHierarchyAndroid?>? =
        null
    var accessibilityCheckResultUtilsStaticMock: MockedStatic<AccessibilityCheckResultUtils?>? =
        null

    var testSubject: ATFAScanner? = null
    var parametersStub: Parameters? = null
    var screenshotStub: BitmapImage? = null
    var filteredResultsStub: MutableList<AccessibilityHierarchyCheckResult?>? = null

    @Before
    fun prepare() {
        accessibilityCheckPresetStaticMock =
            Mockito.mockStatic<AccessibilityCheckPreset?>(AccessibilityCheckPreset::class.java)
        accessibilityHierarchyAndroidStaticMock =
            Mockito.mockStatic<AccessibilityHierarchyAndroid?>(AccessibilityHierarchyAndroid::class.java)
        accessibilityCheckResultUtilsStaticMock =
            Mockito.mockStatic<AccessibilityCheckResultUtils?>(AccessibilityCheckResultUtils::class.java)
        screenshotStub = BitmapImage(bitmapMock)
        parametersStub = Parameters()
        filteredResultsStub = mutableListOf<AccessibilityHierarchyCheckResult?>()
        testSubject = ATFAScanner(contextMock!!)
    }

    @After
    fun cleanUp() {
        accessibilityCheckResultUtilsStaticMock!!.close()
        accessibilityHierarchyAndroidStaticMock!!.close()
        accessibilityCheckPresetStaticMock!!.close()
    }

    @Test
    @Throws(ViewChangedException::class)
    fun scanWithATFAReturnsCorrectResult() {
        accessibilityCheckPresetStaticMock!!
            .`when`<Any?>(
                Verification {
                    AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
                        AccessibilityCheckPreset.LATEST
                    )
                })
            .thenReturn(ImmutableSet.of<AccessibilityHierarchyCheck?>(checkMock))
        accessibilityHierarchyAndroidStaticMock!!
            .`when`<Any?>(
                Verification {
                    AccessibilityHierarchyAndroid.newBuilder(
                        accessibilityNodeInfoMock,
                        contextMock
                    )
                })
            .thenReturn(builderMock)
        Mockito.`when`<AccessibilityHierarchyAndroid?>(builderMock!!.build())
            .thenReturn(hierarchyMock)
        accessibilityCheckResultUtilsStaticMock!!
            .`when`<Any?>(
                Verification {
                    AccessibilityCheckResultUtils.getResultsForTypes<AccessibilityCheckResult?>(
                        ArgumentMatchers.eq<MutableList<AccessibilityCheckResult?>?>(mutableListOf<AccessibilityCheckResult?>()),
                        ArgumentMatchers.anySet<AccessibilityCheckResultType?>()
                    )
                })
            .thenReturn(filteredResultsStub)

        Assert.assertEquals(
            testSubject!!.scanWithATFA(accessibilityNodeInfoMock!!, screenshotStub),
            filteredResultsStub
        )
    }
}
