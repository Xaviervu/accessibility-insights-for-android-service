// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult
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
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ATFAScannerTest {
    @Mock
    lateinit var bitmapMock: Bitmap

    @Mock
    lateinit var accessibilityNodeInfoMock: AccessibilityNodeInfo

    @Mock
    lateinit var contextMock: Context

    @Mock
    lateinit var checkMock: AccessibilityHierarchyCheck

    @Mock
    lateinit var hierarchyMock: AccessibilityHierarchyAndroid

    @Mock
    lateinit var builderMock: AccessibilityHierarchyAndroid.BuilderAndroid

    lateinit var accessibilityCheckPresetStaticMock: MockedStatic<AccessibilityCheckPreset>
    lateinit var accessibilityHierarchyAndroidStaticMock: MockedStatic<AccessibilityHierarchyAndroid>
    lateinit var accessibilityCheckResultUtilsStaticMock: MockedStatic<AccessibilityCheckResultUtils>

    lateinit var testSubject: ATFAScanner
    lateinit var parametersStub: Parameters
    lateinit var screenshotStub: BitmapImage
    lateinit var filteredResultsStub: MutableList<AccessibilityHierarchyCheckResult>

    @Before
    fun prepare() {
        accessibilityCheckPresetStaticMock =
            Mockito.mockStatic(AccessibilityCheckPreset::class.java)
        accessibilityHierarchyAndroidStaticMock =
            Mockito.mockStatic(AccessibilityHierarchyAndroid::class.java)
        accessibilityCheckResultUtilsStaticMock =
            Mockito.mockStatic(AccessibilityCheckResultUtils::class.java)
        screenshotStub = BitmapImage(bitmapMock)
        parametersStub = Parameters()
        filteredResultsStub = mutableListOf()
        testSubject = ATFAScanner(contextMock)
    }

    @After
    fun cleanUp() {
        accessibilityCheckResultUtilsStaticMock.close()
        accessibilityHierarchyAndroidStaticMock.close()
        accessibilityCheckPresetStaticMock.close()
    }

    @Test
    @Throws(ViewChangedException::class)
    fun scanWithATFAReturnsCorrectResult() {
        accessibilityCheckPresetStaticMock
            .`when`<Any?> {
                AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
                    AccessibilityCheckPreset.LATEST,
                )
            }.thenReturn(ImmutableSet.of(checkMock))
        accessibilityHierarchyAndroidStaticMock
            .`when`<Any?> {
                AccessibilityHierarchyAndroid.newBuilder(
                    accessibilityNodeInfoMock,
                    contextMock,
                )
            }.thenReturn(builderMock)
        Mockito
            .`when`(builderMock.build())
            .thenReturn(hierarchyMock)
        accessibilityCheckResultUtilsStaticMock
            .`when`<Any?> {
                AccessibilityCheckResultUtils.getResultsForTypes(
                    ArgumentMatchers.eq(mutableListOf<AccessibilityCheckResult>()),
                    ArgumentMatchers.anySet(),
                )
            }.thenReturn(filteredResultsStub)

        Assert.assertEquals(
            testSubject.scanWithATFA(accessibilityNodeInfoMock, screenshotStub),
            filteredResultsStub,
        )
    }
}
