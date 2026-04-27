// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import com.deque.axe.android.AxeResult
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAResultsSerializer
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFARulesSerializer
import java.io.IOException

class ResultsV2ContainerSerializer(
    private val atfaRulesSerializer: ATFARulesSerializer,
    private val atfaResultsSerializer: ATFAResultsSerializer,
    gsonBuilder: GsonBuilder
) {
    private val gson: Gson
    private val resultsContainerTypeAdapter: TypeAdapter<ResultsV2Container> =
        object : TypeAdapter<ResultsV2Container>() {
            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: ResultsV2Container) {
                out.beginObject()
                out.name("AxeResults").jsonValue(value.AxeResult?.toJson())
                out.name("ATFARules").jsonValue(atfaRulesSerializer.serializeATFARules())
                out.name("ATFAResults")
                    .jsonValue(atfaResultsSerializer.serializeATFAResults(value.ATFAResults))
                out.endObject()
            }

            override fun read(`in`: JsonReader?): ResultsV2Container? {
                return null
            }
        }

    init {
        this.gson =
            gsonBuilder
                .registerTypeAdapter(
                    ResultsV2Container::class.java,
                    this.resultsContainerTypeAdapter
                )
                .create()
    }

    fun createResultsJson(
        axeResult: AxeResult?, atfaResults: MutableList<AccessibilityHierarchyCheckResult?>?
    ): String {
        val container = ResultsV2Container()
        container.ATFAResults = atfaResults
        container.AxeResult = axeResult
        return gson.toJson(container)
    }
}
