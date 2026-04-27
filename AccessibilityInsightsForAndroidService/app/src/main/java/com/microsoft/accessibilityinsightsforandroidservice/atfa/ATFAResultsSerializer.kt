// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice.atfa

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElement
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElementAndroid
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Field
import java.lang.reflect.Type

class ATFAResultsSerializer(
    gsonBuilder: GsonBuilder,
) {
    private val gsonSerializer: Gson =
        gsonBuilder
            .setFieldNamingStrategy(ATFAFieldNamingStrategy)
            .setExclusionStrategies(ATFAExclusionStrategy)
            .registerTypeAdapter(Class::class.java, ClassSerializer)
            .create()

    fun serializeATFAResults(atfaResults: List<AccessibilityHierarchyCheckResult>?): String = gsonSerializer.toJson(atfaResults)

    companion object {
        private val ClassesToSkip: MutableList<Class<*>?> =
            mutableListOf<Class<*>?>(
                WindowHierarchyElement::class.java,
                WindowHierarchyElementAndroid::class.java,
            )

        private val ATFAFieldNamingStrategy =
            FieldNamingStrategy { f: Field ->
                f.declaringClass.getSimpleName() + "." + f.getName()
            }

        private val ATFAExclusionStrategy: ExclusionStrategy =
            object : ExclusionStrategy {
                override fun shouldSkipField(f: FieldAttributes?): Boolean = false

                override fun shouldSkipClass(clazz: Class<*>?): Boolean = ClassesToSkip.contains(clazz)
            }

        private val ClassSerializer =
            JsonSerializer { src: Class<out AccessibilityCheck?>?, _: Type?, _: JsonSerializationContext? ->
                JsonPrimitive(src?.getSimpleName())
            }
    }
}
