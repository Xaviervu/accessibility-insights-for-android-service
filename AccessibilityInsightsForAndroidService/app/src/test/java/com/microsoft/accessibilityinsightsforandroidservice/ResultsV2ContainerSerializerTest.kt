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
    lateinit var axeResultMock: AxeResult

    @Mock
    lateinit var atfaRulesSerializer: ATFARulesSerializer

    @Mock
    lateinit var atfaResultsSerializer: ATFAResultsSerializer

    @Mock
    lateinit var gsonBuilder: GsonBuilder

    @Mock
    lateinit var jsonWriter: JsonWriter

    @Mock
    lateinit var gson: Gson

    val atfaResults: MutableList<AccessibilityHierarchyCheckResult> =
        mutableListOf()
    val resultsV2Container: ResultsV2Container = ResultsV2Container()

    lateinit var resultsContainerTypeAdapter: TypeAdapter<ResultsV2Container>
    lateinit var testSubject: ResultsV2ContainerSerializer

    @Before
    fun prepare() {
        Mockito
            .doAnswer(
                AdditionalAnswers.answer { type: Type?, typeAdapter: TypeAdapter<ResultsV2Container> ->
                    resultsContainerTypeAdapter = typeAdapter
                    gsonBuilder
                },
            ).`when`(gsonBuilder)
            .registerTypeAdapter(
                ArgumentMatchers.eq(ResultsV2Container::class.java),
                ArgumentMatchers.any(),
            )

        Mockito.`when`<Gson?>(gsonBuilder.create()).thenReturn(gson)
        resultsV2Container.AxeResult = axeResultMock
        resultsV2Container.ATFAResults = atfaResults
        testSubject =
            ResultsV2ContainerSerializer(
                atfaRulesSerializer,
                atfaResultsSerializer,
                gsonBuilder,
            )
    }

    @Test
    fun generatesExpectedJson() {
        val resultsContainer = AtomicReference<ResultsV2Container?>()
        Mockito
            .doAnswer(
                AdditionalAnswers.answer(
                    Answer1 { container: ResultsV2Container ->
                        resultsContainer.set(container)
                        "Test String"
                    },
                ),
            ).`when`(gson)
            .toJson(ArgumentMatchers.any(ResultsV2Container::class.java))

        testSubject.createResultsJson(axeResultMock, atfaResults)

        Assert.assertEquals(axeResultMock, resultsContainer.get()?.AxeResult)
        Assert.assertEquals(atfaResults, resultsContainer.get()?.ATFAResults)
    }

    @Test
    @Throws(IOException::class)
    fun typeAdapterSerializes() {
        val axeJson = "axe scan result"
        val atfaRulesJson = "atfa rules"
        val atfaJson = "atfa scan results"

        Mockito.`when`(axeResultMock.toJson()).thenReturn(axeJson)
        Mockito.`when`(atfaRulesSerializer.serializeATFARules()).thenReturn(atfaRulesJson)
        Mockito
            .`when`(atfaResultsSerializer.serializeATFAResults(atfaResults))
            .thenReturn(atfaJson)
        Mockito.`when`(jsonWriter.name("AxeResults")).thenReturn(jsonWriter)
        Mockito.`when`(jsonWriter.name("ATFARules")).thenReturn(jsonWriter)
        Mockito.`when`(jsonWriter.name("ATFAResults")).thenReturn(jsonWriter)

        resultsContainerTypeAdapter.write(jsonWriter, resultsV2Container)

        Mockito.verify(jsonWriter, Mockito.times(1)).beginObject()
        Mockito.verify(jsonWriter, Mockito.times(1)).jsonValue(axeJson)
        Mockito.verify(jsonWriter, Mockito.times(1)).jsonValue(atfaRulesJson)
        Mockito.verify(jsonWriter, Mockito.times(1)).jsonValue(atfaJson)
        Mockito.verify(jsonWriter, Mockito.times(1)).endObject()
        Mockito.verify(axeResultMock, Mockito.times(1)).toJson()
    }
}
