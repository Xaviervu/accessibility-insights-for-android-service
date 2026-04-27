// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ThreadSafeSwapperTest {
    @Mock
    var mockOldObject: GenericTestObject? = null

    @Mock
    var mockIntermediateObject: GenericTestObject? = null

    @Mock
    var mockNewObject: GenericTestObject? = null

    var testSubject: ThreadSafeSwapper<GenericTestObject?>? = null

    @Before
    fun prepare() {
        testSubject = ThreadSafeSwapper<GenericTestObject?>()
    }

    @Test
    fun threadSafeSwapperExists() {
        Assert.assertNotNull(testSubject)
    }

    @Test
    fun swapReturnsSwappedOutObject() {
        testSubject!!.swap(mockOldObject)

        val actualReturnedObject = testSubject!!.swap(mockNewObject)

        Assert.assertEquals(mockOldObject, actualReturnedObject)
    }

    @Test
    fun swapReplacesCurrentObjectWithMethodParameter() {
        testSubject!!.swap(mockOldObject)

        val actualOldObject = testSubject!!.swap(mockIntermediateObject)
        val actualIntermediateObject = testSubject!!.swap(mockNewObject)

        Assert.assertEquals(mockOldObject, actualOldObject)
        Assert.assertEquals(mockIntermediateObject, actualIntermediateObject)
    }

    @Test
    fun setIfCurrentlyNullDoesNotSetCurrentObjectIfNotNull() {
        val expectedReturnValue = false
        testSubject!!.swap(mockOldObject)

        val actualReturnValue = testSubject!!.setIfCurrentlyNull(mockNewObject)

        Assert.assertEquals(expectedReturnValue, actualReturnValue)
    }

    @Test
    fun setIfCurrentlyNullSetsCurrentObjectIfNull() {
        val expectedReturnValue = true
        testSubject!!.swap(null)

        val actualReturnValue = testSubject!!.setIfCurrentlyNull(mockNewObject)

        Assert.assertEquals(expectedReturnValue, actualReturnValue)
    }

    inner class GenericTestObject
}
