// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.concurrent.TimeUnit

class TempFileProvider
    @JvmOverloads
    constructor(
        context: Context,
        private val workManager: WorkManager =
            WorkManagerHolder.getWorkManager(
                context,
            ),
    ) {
        private val tempDir: File

        init {
            val cacheDir = context.cacheDir
            val tempDirPath = cacheDir.absolutePath + File.separator + TEMP_DIR_NAME
            this.tempDir = File(tempDirPath)
        }

        fun cleanOldFilesBestEffort() {
            cleanOldFilesBestEffort(tempDir)
        }

        @Throws(IOException::class)
        fun createTempFileWithContents(contents: String?): File {
            ensureTempDirExists()
            val tempFile = File.createTempFile("TempFileProvider", "tmp", this.tempDir)
            BufferedWriter(
                OutputStreamWriter(FileOutputStream(tempFile), StandardCharsets.UTF_8),
            ).use { writer ->
                writer.write(contents)
                writer.flush()
            }
            scheduleCleanOldFiles(tempDir.absolutePath)
            return tempFile
        }

        @Throws(IOException::class)
        private fun ensureTempDirExists() {
            this.tempDir.mkdir()
        }

        private fun scheduleCleanOldFiles(tempDir: String?) {
            val inputData = Data.Builder().putString("tempDir", tempDir).build()
            val cleanFilesWorker =
                OneTimeWorkRequest
                    .Builder(CleanWorker::class.java)
                    .setInitialDelay(TEMP_FILE_LIFETIME_MILLIS.toLong(), TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()
            workManager.enqueue(cleanFilesWorker)
        }

        class CleanWorker(
            context: Context,
            workerParams: WorkerParameters,
        ) : Worker(context, workerParams) {
            private val tempDir: String = workerParams.inputData.getString("tempDir") ?: ""

            override fun doWork(): Result {
                cleanOldFilesBestEffort(File(tempDir))
                return Result.success()
            }
        }

        companion object {
            // Avoid ever changing this; we want new versions of the app to be able to recognize and clean up
            // old versions'
            private const val TEMP_DIR_NAME =
                "com.microsoft.accessibilityinsightsforandroidservice.TempFileProvider"

            const val TEMP_FILE_LIFETIME_MILLIS: Int = 5 * 60 * 1000 // 5 minutes

            private fun cleanOldFilesBestEffort(tempDir: File) {
                val cutoffTime: Long = Date().time - TEMP_FILE_LIFETIME_MILLIS
                val files = tempDir.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.lastModified() < cutoffTime) {
                            // We intentionally ignore failures (best-effort)
                            // noinspection ResultOfMethodCallIgnored
                            file.delete()
                        }
                    }
                }
            }
        }
    }
