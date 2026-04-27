// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import com.deque.axe.android.AxeResult
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonWriter
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAResultsSerializer
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFARulesSerializer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.Answer1
import org.mockito.stubbing.Answer2
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicReference

@RunWith(MockitoJUnitRunner::class)
class ResultsV2ContainerSerializerTest {
    @Mock
    var axeResultMock: AxeResult? = null

    @Mock
    var atfaRulesSerializer: ATFARulesSerializer? = null

    @Mock
    var atfaResultsSerializer: ATFAResultsSerializer? = null

    @Mock
    var gsonBuilder: GsonBuilder? = null

    @Mock
    var jsonWriter: JsonWriter? = null

    @Mock
    var gson: Gson? = null

    val atfaResults: MutableList<AccessibilityHierarchyCheckResult?> =
        mutableListOf<AccessibilityHierarchyCheckResult?>()
    val resultsV2Container: ResultsV2Container = ResultsV2Container()

    var resultsContainerTypeAdapter: TypeAdapter<ResultsV2Container?>? = null
    var testSubject: ResultsV2ContainerSerializer? = null

    @Before
    fun prepare() {
        Mockito.doAnswer(
            AdditionalAnswers.answer<GsonBuilder?, Type?, TypeAdapter<ResultsV2Container?>?>(
                Answer2 { type: Type?, typeAdapter: TypeAdapter<ResultsV2Container?> ->
                    resultsContainerTypeAdapter = typeAdapter
                    gsonBuilder
                })
        )
            .`when`<GsonBuilder?>(gsonBuilder)
            .registerTypeAdapter(
                ArgumentMatchers.eq<Class<ResultsV2Container?>?>(ResultsV2Container::class.java),
                ArgumentMatchers.any<Any?>()
            )

        Mockito.`when`<Gson?>(gsonBuilder!!.create()).thenReturn(gson)
        resultsV2Container.AxeResult = axeResultMock
        resultsV2Container.ATFAResults = atfaResults
        testSubject =
            ResultsV2ContainerSerializer(
                atfaRulesSerializer!!,
                atfaResultsSerializer!!,
                gsonBuilder!!
            )
    }

    @Test
    fun generatesExpectedJson() {
        val resultsContainer = AtomicReference<ResultsV2Container?>()
        Mockito.doAnswer(
            AdditionalAnswers.answer<String?, ResultsV2Container?>(
                Answer1 { container: ResultsV2Container? ->
                    resultsContainer.set(container)
                    "Test String"
                })
        )
            .`when`<Gson?>(gson)
            .toJson(ArgumentMatchers.any<ResultsV2Container?>(ResultsV2Container::class.java))

        testSubject!!.createResultsJson(axeResultMock, atfaResults)

        Assert.assertEquals(axeResultMock, resultsContainer.get()!!.AxeResult)
        Assert.assertEquals(atfaResults, resultsContainer.get()!!.ATFAResults)
    }

    @Test
    @Throws(IOException::class)
    fun typeAdapterSerializes() {
        val axeJson = "axe scan result"
        val atfaRulesJson = "atfa rules"
        val atfaJson = "atfa scan results"

        Mockito.`when`<String?>(axeResultMock!!.toJson()).thenReturn(axeJson)
        Mockito.`when`<String>(atfaRulesSerializer!!.serializeATFARules()).thenReturn(atfaRulesJson)
        Mockito.`when`<String>(atfaResultsSerializer!!.serializeATFAResults(atfaResults))
            .thenReturn(atfaJson)
        Mockito.`when`<JsonWriter?>(jsonWriter!!.name("AxeResults")).thenReturn(jsonWriter)
        Mockito.`when`<JsonWriter?>(jsonWriter!!.name("ATFARules")).thenReturn(jsonWriter)
        Mockito.`when`<JsonWriter?>(jsonWriter!!.name("ATFAResults")).thenReturn(jsonWriter)

        resultsContainerTypeAdapter!!.write(jsonWriter, resultsV2Container)

        Mockito.verify<JsonWriter?>(jsonWriter, Mockito.times(1)).beginObject()
        Mockito.verify<JsonWriter?>(jsonWriter, Mockito.times(1)).jsonValue(axeJson)
        Mockito.verify<JsonWriter?>(jsonWriter, Mockito.times(1)).jsonValue(atfaRulesJson)
        Mockito.verify<JsonWriter?>(jsonWriter, Mockito.times(1)).jsonValue(atfaJson)
        Mockito.verify<JsonWriter?>(jsonWriter, Mockito.times(1)).endObject()
        Mockito.verify<AxeResult?>(axeResultMock, Mockito.times(1)).toJson()
    }
}
