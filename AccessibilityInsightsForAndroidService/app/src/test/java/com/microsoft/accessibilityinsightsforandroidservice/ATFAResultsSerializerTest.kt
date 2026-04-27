// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElement
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElementAndroid
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAResultsSerializer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.lang.reflect.Field
import java.util.Arrays
import java.util.function.Consumer
import java.util.stream.Collectors

@RunWith(MockitoJUnitRunner::class)
class ATFAResultsSerializerTest {
    var gsonBuilder: GsonBuilder =
        Mockito.mock<GsonBuilder>(GsonBuilder::class.java, Mockito.RETURNS_SELF)

    @Mock
    var gson: Gson? = null

    @Captor
    var fieldNamingStrategy: ArgumentCaptor<FieldNamingStrategy?>? = null

    @Captor
    var exclusionStrategy: ArgumentCaptor<ExclusionStrategy?>? = null

    @Captor
    var jsonSerializer: ArgumentCaptor<JsonSerializer<Class<*>?>?>? = null
    var testSubject: ATFAResultsSerializer? = null

    internal open inner class TestClass {
        var testField: String? = null
    }

    @Before
    fun prepare() {
        Mockito.`when`<Gson?>(gsonBuilder.create()).thenReturn(gson)

        testSubject = ATFAResultsSerializer(gsonBuilder)

        Mockito.verify<GsonBuilder?>(gsonBuilder)
            .setExclusionStrategies(exclusionStrategy!!.capture())
        Mockito.verify<GsonBuilder?>(gsonBuilder)
            .setFieldNamingStrategy(fieldNamingStrategy!!.capture())
        Mockito.verify<GsonBuilder?>(gsonBuilder).registerTypeAdapter(
            ArgumentMatchers.eq<Class<Class<*>?>?>(
                Class::class.java
            ), jsonSerializer!!.capture()
        )
    }

    @Test
    fun fieldNamingStrategyReturnsName() {
        class ExtendingClass : TestClass() {
            var testField: String? = null
        }

        val testFields = ExtendingClass::class.java.getFields()

        val testFieldExtendingClass =
            fieldNamingStrategy!!.getValue()!!.translateName(testFields[0])
        val testFieldBaseClass = fieldNamingStrategy!!.getValue()!!.translateName(testFields[1])

        Assert.assertEquals("TestClass.testField", testFieldBaseClass)
        Assert.assertEquals("ExtendingClass.testField", testFieldExtendingClass)
    }

    @Test
    fun exclusionStrategyExcludesWindowHierarchyElements() {
        val classesToExclude =
            Arrays.asList<Class<*>?>(
                WindowHierarchyElement::class.java,
                WindowHierarchyElementAndroid::class.java
            )
        val classesToInclude: MutableList<Class<*>?> =
            Arrays.stream<Field?>(ViewHierarchyElement::class.java.getFields())
                .map { f: Field? -> f!!.javaClass }
                .filter { c: Class<Field?>? -> !classesToExclude.contains(c) }
                .collect(Collectors.toList())

        classesToExclude.forEach(
            Consumer { c: Class<*>? ->
                Assert.assertTrue(
                    exclusionStrategy!!.getValue()!!.shouldSkipClass(c)
                )
            })
        classesToInclude.forEach(
            Consumer { c: Class<*>? ->
                Assert.assertFalse(
                    exclusionStrategy!!.getValue()!!.shouldSkipClass(c)
                )
            })
    }

    @Test
    fun jsonSerializerSerializesClassName() {
        val expectedJson = JsonPrimitive(TestClass::class.java.getSimpleName())

        val jsonElement =
            jsonSerializer!!.getValue()!!.serialize(TestClass::class.java, Class::class.java, null)

        Assert.assertEquals(expectedJson, jsonElement)
    }

    @Test
    fun serializeATFAResultsCallsGsonSerializer() {
        val results = mutableListOf<AccessibilityHierarchyCheckResult?>()

        testSubject!!.serializeATFAResults(results)

        Mockito.verify<Gson?>(gson, Mockito.times(1)).toJson(results)
    }
}
