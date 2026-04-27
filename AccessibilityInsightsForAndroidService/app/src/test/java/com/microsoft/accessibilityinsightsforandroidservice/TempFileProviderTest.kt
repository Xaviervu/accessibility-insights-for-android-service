// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.impl.model.WorkSpec
import androidx.work.impl.utils.taskexecutor.WorkManagerTaskExecutor
import com.google.common.util.concurrent.Futures
import com.microsoft.accessibilityinsightsforandroidservice.TempFileProvider.CleanWorker
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.Date
import java.util.UUID
import java.util.concurrent.Executors

@RunWith(MockitoJUnitRunner::class)
class TempFileProviderTest {
    @Mock
    lateinit var contextMock: Context

    @Mock
    lateinit var workManagerMock: WorkManager

    @Captor
    lateinit var workRequestCaptor: ArgumentCaptor<WorkRequest>

    lateinit var testSubject: TempFileProvider
    lateinit var cacheDirectory: File

    fun makeFileLookOld(file: File) {
        // We subtract one second because setLastModified is documented as being accurate to 1s
        file.setLastModified(
            Date().time - TempFileProvider.TEMP_FILE_LIFETIME_MILLIS - 60 * 1000,
        )
    }

    @Before
    @Throws(Exception::class)
    fun prepare() {
        cacheDirectory = Files.createTempDirectory("tempFileProviderTest").toFile()
        Mockito.`when`(contextMock.cacheDir).thenReturn(cacheDirectory)
        testSubject = TempFileProvider(contextMock, workManagerMock)
    }

    @After
    fun cleanUp() {
        if (cacheDirectory.exists()) {
            cacheDirectory.delete()
        }
    }

    @Test
    @Throws(IOException::class)
    fun tempFileIsCreatedWithContent() {
        // Check the encoding
        val content = "Test string"
        val tempFile = testSubject.createTempFileWithContents(content)
        val fileContent = Files.readAllBytes(tempFile.toPath())
        Assert.assertArrayEquals(content.toByteArray(StandardCharsets.UTF_8), fileContent)
    }

    @Test
    @Throws(IOException::class)
    fun tempFileCreateWithSpecialCharactersInContent() {
        val specialCharacters = "\uD83E\uDD8F \uD83D\uDE1F \uD83D\uDC39 ⌛️"
        val tempFile = testSubject.createTempFileWithContents(specialCharacters)
        val fileContent = Files.readAllBytes(tempFile.toPath())
        Assert.assertArrayEquals(specialCharacters.toByteArray(StandardCharsets.UTF_8), fileContent)
    }

    @Test
    @Throws(IOException::class)
    fun createTempFileWithContentsThrowsIOExceptionIfTempFileCanNotBeCreated() {
        Mockito.mockStatic<File?>(File::class.java).use { fileStaticMock ->
            fileStaticMock
                .`when`<Any?>(
                    Verification {
                        File.createTempFile(
                            ArgumentMatchers.any(),
                            ArgumentMatchers.any(),
                            ArgumentMatchers.any(),
                        )
                    },
                ).thenThrow(IOException())
            Assert.assertThrows(
                IOException::class.java,
            ) { testSubject.createTempFileWithContents("Content") }
        }
    }

    @Test
    @Throws(IOException::class)
    fun createTempFileWithContentsCreatesANewFileEveryTime() {
        val firstTempFile = testSubject.createTempFileWithContents("Test string")
        val secondTempFile = testSubject.createTempFileWithContents("Test string")
        val firstPath = firstTempFile.absolutePath
        val secondPath = secondTempFile.absolutePath
        Assert.assertNotEquals(firstPath, secondPath)
    }

    @Test
    @Throws(IOException::class)
    fun createTempFileWithContentCreatesFileUnderCacheDir() {
        val output = testSubject.createTempFileWithContents("Test string")
        val path = output.absolutePath
        Assert.assertTrue(path.startsWith(cacheDirectory.absolutePath))
    }

    @Test
    @Throws(IOException::class)
    fun cleanOldFilesOnlyDeletesOldFiles() {
        val oldFile = testSubject.createTempFileWithContents("Old File")
        val newFile = testSubject.createTempFileWithContents("New File")
        makeFileLookOld(oldFile)
        testSubject.cleanOldFilesBestEffort()
        Assert.assertFalse(oldFile.exists())
        Assert.assertTrue(newFile.exists())
    }

    @Test
    fun cleanOldFilesNoopsIfTempDirIsDeleted() {
        cacheDirectory.delete()
        testSubject.cleanOldFilesBestEffort() // Should not throw exception
    }

    @Test
    @Throws(IOException::class)
    fun cleanOldFilesContinuesIfAFileCanNotBeDeleted() {
        val erasableFile = testSubject.createTempFileWithContents("Test string")
        val noErasableFile = testSubject.createTempFileWithContents("Test sting")
        makeFileLookOld(erasableFile)
        makeFileLookOld(noErasableFile)
        BufferedWriter(FileWriter(noErasableFile)).use {
            testSubject.cleanOldFilesBestEffort()
        }
        Assert.assertTrue(noErasableFile.exists())
        Assert.assertFalse(erasableFile.exists())
    }

    @Test
    @Throws(IOException::class)
    fun createTempFileSchedulesACleanWorker() {
        val oldFile = testSubject.createTempFileWithContents("Old File")
        makeFileLookOld(oldFile)

        val workSpec = this.lastWorkManagerRequest
        Assert.assertEquals(
            TempFileProvider.TEMP_FILE_LIFETIME_MILLIS.toLong(),
            workSpec.initialDelay,
        )
        Assert.assertEquals(CleanWorker::class.java.getName(), workSpec.workerClassName)

        val workerParameters = createStubWorkerParameters(workSpec)
        val cleanWorker =
            CleanWorker(contextMock, workerParameters)
        val result = cleanWorker.doWork()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
        Assert.assertFalse(oldFile.exists())
    }

    private fun createStubWorkerParameters(workSpec: WorkSpec): WorkerParameters {
        val inputData = workSpec.input
        val executor = Executors.newSingleThreadExecutor()
        val workerFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters,
            ): ListenableWorker? = null
        }
        val workerParameters =
            WorkerParameters(
                UUID.randomUUID(),
                inputData,
                ArrayList<String>(),
                WorkerParameters.RuntimeExtras(),
                1,
                0,
                executor,
                Dispatchers.Default,
                WorkManagerTaskExecutor(executor),
                workerFactory,
                { _, _, _ -> Futures.immediateFuture(null) },
                { _, _, _ -> Futures.immediateFuture(null) },
            )
        return workerParameters
    }

    private val lastWorkManagerRequest: WorkSpec
        get() {
            Mockito
                .verify(workManagerMock, Mockito.times(1))
                .enqueue(workRequestCaptor.capture())
            val workSpec = workRequestCaptor.getValue().workSpec
            return workSpec
        }
}
