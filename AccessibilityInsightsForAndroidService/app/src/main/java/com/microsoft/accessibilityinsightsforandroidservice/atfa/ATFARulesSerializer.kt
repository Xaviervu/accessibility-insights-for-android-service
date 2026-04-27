// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice.atfa

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck
import com.google.common.collect.ImmutableSet
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.Type
import java.util.Arrays
import java.util.Locale
import java.util.regex.Pattern

class ATFARulesSerializer {
    fun serializeATFARules(): String {
        val gsonBuilder = GsonBuilder()
        val gsonSerializer =
            gsonBuilder
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapterFactory(AccessibilityHierarchyCheckAdapterFactory())
                .create()

        val presetChecks: ImmutableSet<AccessibilityHierarchyCheck> =
            AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
                AccessibilityCheckPreset.LATEST,
            )

        return gsonSerializer.toJson(presetChecks)
    }

    private fun serializeRuleIdsAndMetadata(check: AccessibilityHierarchyCheck?): String {
        val gson =
            GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .excludeFieldsWithModifiers()
                .setExclusionStrategies(ATFARuleExclusionStrategy)
                .registerTypeAdapter(Class::class.java, ClassSerializer)
                .create()
        return gson.toJson(check)
    }

    private inner class AccessibilityHierarchyCheckAdapterFactory : TypeAdapterFactory {
        override fun <T> create(
            gson: Gson?,
            type: TypeToken<T?>,
        ): TypeAdapter<T?>? {
            if (!AccessibilityHierarchyCheck::class.java.isAssignableFrom(type.getRawType())) return null

            return AccessibilityHierarchyCheckAdapter() as TypeAdapter<T?>
        }
    }

    private inner class AccessibilityHierarchyCheckAdapter : TypeAdapter<AccessibilityHierarchyCheck>() {
        @Throws(IOException::class)
        override fun write(
            out: JsonWriter,
            value: AccessibilityHierarchyCheck,
        ) {
            out.beginObject()
            out.name("class").value(value.javaClass.getName())
            out.name("titleMessage").value(value.getTitleMessage(Locale.getDefault()))
            out.name("category").value(value.getCategory().toString())
            out.name("helpUrl").value(value.getHelpUrl())
            out.name("resultIdsAndMetadata").jsonValue(serializeRuleIdsAndMetadata(value))
            out.endObject()
        }

        @Throws(IOException::class)
        override fun read(`in`: JsonReader?): AccessibilityHierarchyCheck? = null
    }

    companion object {
        private val FieldsToSkip = mutableListOf<String?>("ANDROID_A11Y_HELP_URL")

        private val ClassesToSkip: MutableList<Class<*>?> =
            Arrays.asList<Class<*>?>(Pattern::class.java)

        private val ATFARuleExclusionStrategy: ExclusionStrategy =
            object : ExclusionStrategy {
                override fun shouldSkipField(f: FieldAttributes): Boolean = FieldsToSkip.contains(f.getName())

                override fun shouldSkipClass(clazz: Class<*>?): Boolean = ClassesToSkip.contains(clazz)
            }

        private val ClassSerializer =
            JsonSerializer { src: Class<out AccessibilityCheck?>?, typeOfSrc: Type?, context: JsonSerializationContext? ->
                JsonPrimitive(src!!.getSimpleName())
            }
    }
}
