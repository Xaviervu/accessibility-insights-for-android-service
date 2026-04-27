// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.OngoingStubbing
import java.io.BufferedReader
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class RequestReaderTest {
    @Mock
    var bufferedReaderMock: BufferedReader? = null

    var testSubject: RequestReader? = null

    @Before
    fun prepare() {
        testSubject = RequestReader(bufferedReaderMock!!)
    }

    @Test
    fun requestReaderExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    @Throws(IOException::class)
    fun readsFromBufferedReader() {
        val requestString = "test request string"
        setupReadLine(requestString)

        val actualRequestString = testSubject!!.readRequest()

        Assert.assertEquals(actualRequestString, requestString)
    }

    @Test
    @Throws(IOException::class)
    fun limitsInputLength() {
        var bufferedReaderStubbing = Mockito.`when`<Int?>(bufferedReaderMock!!.read())
        for (i in 0..299) {
            bufferedReaderStubbing = bufferedReaderStubbing.thenReturn(42)
        }

        try {
            testSubject!!.readRequest()
            Assert.fail("Should have thrown exception")
        } catch (e: IOException) {
            Assert.assertEquals(e.message, "input too long")
        }
    }

    private fun setupReadLine(str: String) {
        var bufferedReaderStubbing: OngoingStubbing<Int?>
        try {
            bufferedReaderStubbing = Mockito.`when`<Int?>(bufferedReaderMock!!.read())
        } catch (e: IOException) {
            Assert.fail(e.message)
            return
        }

        for (i in 0..<str.length) {
            bufferedReaderStubbing = bufferedReaderStubbing.thenReturn(str.get(i).code)
        }

        bufferedReaderStubbing.thenReturn('\n'.code)
    }
}
