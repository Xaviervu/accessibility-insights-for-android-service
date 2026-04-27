// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.impl.model.WorkSpec
import com.microsoft.accessibilityinsightsforandroidservice.TempFileProvider.CleanWorker
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.function.ThrowingRunnable
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

@RunWith(MockitoJUnitRunner::class)
class TempFileProviderTest {
    @Mock
    var contextMock: Context? = null

    @Mock
    var workManagerMock: WorkManager? = null

    @Captor
    var workRequestCaptor: ArgumentCaptor<WorkRequest?>? = null

    var testSubject: TempFileProvider? = null
    var cacheDirectory: File? = null

    fun makeFileLookOld(file: File) {
        // We subtract one second because setLastModified is documented as being accurate to 1s
        file.setLastModified(
            Date().getTime() - TempFileProvider.TEMP_FILE_LIFETIME_MILLIS - 60 * 1000
        )
    }

    @Before
    @Throws(Exception::class)
    fun prepare() {
        cacheDirectory = Files.createTempDirectory("tempFileProviderTest").toFile()
        Mockito.`when`<File?>(contextMock!!.getCacheDir()).thenReturn(cacheDirectory)
        testSubject = TempFileProvider(contextMock!!, workManagerMock!!)
    }

    @After
    fun cleanUp() {
        if (cacheDirectory!!.exists()) {
            cacheDirectory!!.delete()
        }
    }

    @Test
    @Throws(IOException::class)
    fun tempFileIsCreatedWithContent() {
        // Check the encoding
        val content = "Test string"
        val tempFile = testSubject!!.createTempFileWithContents(content)
        val fileContent = Files.readAllBytes(tempFile.toPath())
        Assert.assertArrayEquals(content.toByteArray(StandardCharsets.UTF_8), fileContent)
    }

    @Test
    @Throws(IOException::class)
    fun tempFileCreateWithSpecialCharactersInContent() {
        val specialCharacters = "\uD83E\uDD8F \uD83D\uDE1F \uD83D\uDC39 ⌛️"
        val tempFile = testSubject!!.createTempFileWithContents(specialCharacters)
        val fileContent = Files.readAllBytes(tempFile.toPath())
        Assert.assertArrayEquals(specialCharacters.toByteArray(StandardCharsets.UTF_8), fileContent)
    }

    @Test
    @Throws(IOException::class)
    fun createTempFileWithContentsThrowsIOExceptionIfTempFileCanNotBeCreated() {
        Mockito.mockStatic<File?>(File::class.java).use { fileStaticMock ->
            fileStaticMock
                .`when`<Any?>(Verification {
                    File.createTempFile(
                        ArgumentMatchers.any<String?>(),
                        ArgumentMatchers.any<String?>(),
                        ArgumentMatchers.any<File?>()
                    )
                })
                .thenThrow(IOException())
            Assert.assertThrows<IOException?>(
                IOException::class.java,
                ThrowingRunnable { testSubject!!.createTempFileWithContents("Content") })
        }
    }

    @Test
    @Throws(IOException::class)
    fun createTempFileWithContentsCreatesANewFileEveryTime() {
        val firstTempFile = testSubject!!.createTempFileWithContents("Test string")
        val secondTempFile = testSubject!!.createTempFileWithContents("Test string")
        val firstPath = firstTempFile.getAbsolutePath()
        val secondPath = secondTempFile.getAbsolutePath()
        Assert.assertNotEquals(firstPath, secondPath)
    }

    @Test
    @Throws(IOException::class)
    fun createTempFileWithContentCreatesFileUnderCacheDir() {
        val output = testSubject!!.createTempFileWithContents("Test string")
        val path = output.getAbsolutePath()
        Assert.assertTrue(path.startsWith(cacheDirectory!!.getAbsolutePath()))
    }

    @Test
    @Throws(IOException::class)
    fun cleanOldFilesOnlyDeletesOldFiles() {
        val oldFile = testSubject!!.createTempFileWithContents("Old File")
        val newFile = testSubject!!.createTempFileWithContents("New File")
        makeFileLookOld(oldFile)
        testSubject!!.cleanOldFilesBestEffort()
        Assert.assertFalse(oldFile.exists())
        Assert.assertTrue(newFile.exists())
    }

    @Test
    fun cleanOldFilesNoopsIfTempDirIsDeleted() {
        cacheDirectory!!.delete()
        testSubject!!.cleanOldFilesBestEffort() // Should not throw exception
    }

    @Test
    @Throws(IOException::class)
    fun cleanOldFilesContinuesIfAFileCanNotBeDeleted() {
        val erasableFile = testSubject!!.createTempFileWithContents("Test string")
        val noErasableFile = testSubject!!.createTempFileWithContents("Test sting")
        makeFileLookOld(erasableFile)
        makeFileLookOld(noErasableFile)
        BufferedWriter(FileWriter(noErasableFile)).use { writer ->
            testSubject!!.cleanOldFilesBestEffort()
        }
        Assert.assertTrue(noErasableFile.exists())
        Assert.assertFalse(erasableFile.exists())
    }

    @Test
    @Throws(IOException::class)
    fun createTempFileSchedulesACleanWorker() {
        val oldFile = testSubject!!.createTempFileWithContents("Old File")
        makeFileLookOld(oldFile)

        val workSpec = this.lastWorkManagerRequest
        Assert.assertEquals(
            TempFileProvider.TEMP_FILE_LIFETIME_MILLIS.toLong(),
            workSpec.initialDelay
        )
        Assert.assertEquals(CleanWorker::class.java.getName(), workSpec.workerClassName)

        val workerParameters = createStubWorkerParameters(workSpec)
        val cleanWorker =
            CleanWorker(contextMock!!, workerParameters)
        val result = cleanWorker.doWork()
        Assert.assertEquals(ListenableWorker.Result.success(), result)
        Assert.assertFalse(oldFile.exists())
    }

    private fun createStubWorkerParameters(workSpec: WorkSpec): WorkerParameters {
        val inputData = workSpec.input
        val workerParameters =
            WorkerParameters(
                null, inputData, ArrayList<Any?>(), null, 1, null, null, null, null, null
            )
        return workerParameters
    }

    private val lastWorkManagerRequest: WorkSpec
        get() {
            Mockito.verify<WorkManager?>(workManagerMock, Mockito.times(1))
                .enqueue(workRequestCaptor!!.capture()!!)
            val workSpec = workRequestCaptor!!.getValue()!!.workSpec
            return workSpec
        }
}
