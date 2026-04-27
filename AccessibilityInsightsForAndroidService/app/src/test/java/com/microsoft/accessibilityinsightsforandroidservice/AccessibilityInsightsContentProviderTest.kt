// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.function.ThrowingRunnable
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class AccessibilityInsightsContentProviderTest {
    @Mock
    var uriMock: Uri? = null

    @Mock
    var cancellationSignalMock: CancellationSignal? = null

    @Mock
    var tempFileProviderMock: TempFileProvider? = null

    @Mock
    var requestDispatcherMock: SynchronizedRequestDispatcher? = null

    @Mock
    var tempFileDescriptor: ParcelFileDescriptor? = null

    @Mock
    var tempFileMock: File? = null

    var binderStaticMock: MockedStatic<Binder?>? = null
    var parcelFileDescriptorStaticMock: MockedStatic<ParcelFileDescriptor?>? = null
    var bundleConstructionMock: MockedConstruction<Bundle?>? = null

    var testSubject: AccessibilityInsightsContentProvider? = null

    @Before
    @Throws(Exception::class)
    fun prepare() {
        binderStaticMock = Mockito.mockStatic<Binder?>(Binder::class.java)
        parcelFileDescriptorStaticMock =
            Mockito.mockStatic<ParcelFileDescriptor?>(ParcelFileDescriptor::class.java)
        bundleConstructionMock = Mockito.mockConstruction<Bundle?>(Bundle::class.java)

        testSubject = AccessibilityInsightsContentProvider()
        Assert.assertTrue(testSubject!!.onCreate(requestDispatcherMock!!, tempFileProviderMock!!))

        parcelFileDescriptorStaticMock!!
            .`when`<Any?>(
                Verification {
                    ParcelFileDescriptor.open(
                        tempFileMock,
                        ParcelFileDescriptor.MODE_READ_ONLY,
                    )
                },
            ).thenReturn(tempFileDescriptor)
    }

    @After
    fun cleanUp() {
        bundleConstructionMock!!.close()
        parcelFileDescriptorStaticMock!!.close()
        binderStaticMock!!.close()
    }

    private fun setupCallerAsAdb() {
        setupCallerAsUid(2000)
    }

    private fun setupCallerAsNotAdb() {
        setupCallerAsUid(1)
    }

    private fun setupCallerAsUid(uid: Int) {
        Mockito.`when`<Int?>(Binder.getCallingUid()).thenReturn(uid)
    }

    @Test
    fun callEmitsBundleWithSerializedSecurityExceptionIfNotAdb() {
        setupCallerAsNotAdb()
        Assert.assertThrows<SecurityException?>(
            SecurityException::class.java,
            ThrowingRunnable { testSubject!!.call("METHOD", "ARG", null) },
        )
    }

    @Test
    fun openFileThrowsSecurityExceptionIfCallerIsNotAdb() {
        setupCallerAsNotAdb()
        Assert.assertThrows<SecurityException?>(
            SecurityException::class.java,
            ThrowingRunnable { testSubject!!.openFile(uriMock!!, "r", null) },
        )
    }

    @Test
    fun openFileDoesNotThrowSecurityExceptionIfCallerIsAdb() {
        setupCallerAsAdb()
        testSubject!!.openFile(uriMock!!, "r", null)
    }

    @Test
    @Throws(Exception::class)
    fun openFileEmitsTempFileWithResponseFromDispatcher() {
        setupCallerAsAdb()
        val dispatcherResponse = "dispatcher response"
        Mockito.`when`<String?>(uriMock!!.getPath()).thenReturn("/uri-path")
        val expectedMethod = "/uri-path"
        Mockito
            .`when`<String?>(
                requestDispatcherMock!!.request(
                    expectedMethod,
                    cancellationSignalMock!!,
                ),
            ).thenReturn(dispatcherResponse)
        Mockito
            .`when`<File>(tempFileProviderMock!!.createTempFileWithContents(ArgumentMatchers.any<String?>()))
            .thenReturn(tempFileMock)
        Assert.assertSame(
            tempFileDescriptor,
            testSubject!!.openFile(uriMock!!, "r", cancellationSignalMock),
        )
        Mockito
            .verify<TempFileProvider?>(tempFileProviderMock)
            .createTempFileWithContents(dispatcherResponse)
    }

    @Test
    @Throws(Exception::class)
    fun openFileEmitsTempFileWithSerializedExceptionOnDispatcherError() {
        setupCallerAsAdb()
        Mockito.`when`<String?>(uriMock!!.getPath()).thenReturn("/uri-path")
        val expectedMethod = "/uri-path"
        Mockito
            .`when`<String?>(
                requestDispatcherMock!!.request(
                    expectedMethod,
                    cancellationSignalMock!!,
                ),
            ).thenThrow(Exception("dispatcher error"))
        Mockito
            .`when`<File>(tempFileProviderMock!!.createTempFileWithContents(ArgumentMatchers.any<String?>()))
            .thenReturn(tempFileMock)
        Assert.assertSame(
            tempFileDescriptor,
            testSubject!!.openFile(uriMock!!, "r", cancellationSignalMock),
        )

        val serializedException = "java.lang.Exception: dispatcher error"
        Mockito
            .verify<TempFileProvider?>(tempFileProviderMock)
            .createTempFileWithContents(serializedException)
    }

    @Test
    @Throws(Exception::class)
    fun openFileThrowsRuntimeExceptionOnTempFileError() {
        setupCallerAsAdb()

        Mockito.`when`<String?>(uriMock!!.getPath()).thenReturn("uri-path")
        Mockito
            .`when`<File>(tempFileProviderMock!!.createTempFileWithContents(ArgumentMatchers.any<String?>()))
            .thenThrow(IOException("tempFileProvider error"))

        Assert.assertThrows<RuntimeException?>(
            "tempFileProvider error",
            RuntimeException::class.java,
            ThrowingRunnable { testSubject!!.openFile(uriMock!!, "r", cancellationSignalMock) },
        )
    }

    @Test
    @Throws(Exception::class)
    fun callEmitsBundleWithResponseFromDispatcher() {
        setupCallerAsAdb()
        val dispatcherResponse = "dispatcher response"
        val expectedMethod = "/method"
        Mockito
            .`when`<String?>(
                requestDispatcherMock!!.request(
                    ArgumentMatchers.eq<String?>(
                        expectedMethod,
                    ),
                    ArgumentMatchers.notNull<CancellationSignal>(),
                ),
            ).thenReturn(dispatcherResponse)

        val returnedBundle = testSubject!!.call("method", null, null)

        Assert.assertEquals(1, bundleConstructionMock!!.constructed().size.toLong())
        Assert.assertSame(returnedBundle, bundleConstructionMock!!.constructed().get(0))
        Mockito.verify<Bundle?>(returnedBundle).putString("response", dispatcherResponse)
    }

    @Test
    @Throws(Exception::class)
    fun callEmitsBundleWithSerializedExceptionOnDispatcherError() {
        setupCallerAsAdb()
        val expectedMethod = "/method"
        Mockito
            .`when`<String?>(
                requestDispatcherMock!!.request(
                    ArgumentMatchers.eq<String?>(
                        expectedMethod,
                    ),
                    ArgumentMatchers.notNull<CancellationSignal>(),
                ),
            ).thenThrow(Exception("dispatcher error"))

        val returnedBundle = testSubject!!.call("method", null, null)

        Assert.assertEquals(1, bundleConstructionMock!!.constructed().size.toLong())
        Assert.assertSame(returnedBundle, bundleConstructionMock!!.constructed().get(0))
        val serializedException = "java.lang.Exception: dispatcher error"
        Mockito.verify<Bundle?>(returnedBundle).putString("response", serializedException)
    }
}
