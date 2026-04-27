// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.Parameters
import com.google.android.apps.common.testing.accessibility.framework.ResultMetadata
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement
import com.google.common.collect.ImmutableSet
import com.google.gson.JsonParser
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFARulesSerializer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.Locale
import java.util.regex.Pattern

@RunWith(MockitoJUnitRunner::class)
class ATFARulesSerializerTest {
    var accessibilityCheckPresetStaticMock: MockedStatic<AccessibilityCheckPreset?>? = null

    var testSubject: ATFARulesSerializer? = null

    internal inner class TestCheckClass : AccessibilityHierarchyCheck() {
        private val TEST_PATTERN_TO_BE_SKIPPED: Pattern = Pattern.compile("")

        override fun getHelpTopic(): String? {
            return "test-help-topic"
        }

        override fun getCategory(): Category {
            return Category.IMPLEMENTATION
        }

        override fun getTitleMessage(locale: Locale?): String {
            return "test-title-message"
        }

        override fun getMessageForResultData(
            locale: Locale?,
            i: Int,
            resultMetadata: ResultMetadata?
        ): String? {
            return null
        }

        override fun getShortMessageForResultData(
            locale: Locale?, i: Int, resultMetadata: ResultMetadata?
        ): String? {
            return null
        }

        override fun runCheckOnHierarchy(
            accessibilityHierarchy: AccessibilityHierarchy?,
            viewHierarchyElement: ViewHierarchyElement?,
            parameters: Parameters?
        ): MutableList<AccessibilityHierarchyCheckResult?>? {
            return null
        }

        companion object {
            private const val ANDROID_A11Y_HELP_URL = "excluded from serialized rule"
            private const val TEST_RESULT_ID = "test result id included in serialized rule"
        }
    }

    @Before
    fun prepare() {
        accessibilityCheckPresetStaticMock =
            Mockito.mockStatic<AccessibilityCheckPreset?>(AccessibilityCheckPreset::class.java)
        testSubject = ATFARulesSerializer()
    }

    @After
    fun cleanUp() {
        accessibilityCheckPresetStaticMock!!.close()
    }

    @Test
    fun serializeATFARulesReturnsExpectedRules() {
        val checkStub = TestCheckClass()

        val expectedSerializedRules =
            ("[\n"
                    + "  {\n"
                    + "    \"class\": \"com.microsoft.accessibilityinsightsforandroidservice.ATFARulesSerializerTest\$TestCheckClass\",\n"
                    + "    \"titleMessage\": \"test-title-message\",\n"
                    + "    \"category\": \"IMPLEMENTATION\",\n"
                    + "    \"helpUrl\": \"https://support.google.com/accessibility/android/answer/test-help-topic\",\n"
                    + "    \"resultIdsAndMetadata\": {\n"
                    + "       \"TEST_RESULT_ID\": \"test result id included in serialized rule\"\n"
                    + "      }\n"
                    + "  }\n"
                    + "]")

        accessibilityCheckPresetStaticMock!!
            .`when`<Any?>(
                Verification {
                    AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
                        AccessibilityCheckPreset.LATEST
                    )
                })
            .thenReturn(ImmutableSet.of<TestCheckClass?>(checkStub))

        val actualSerializedRules = testSubject!!.serializeATFARules()

        Assert.assertEquals(
            JsonParser.parseString(expectedSerializedRules),
            JsonParser.parseString(actualSerializedRules)
        )
    }
}
