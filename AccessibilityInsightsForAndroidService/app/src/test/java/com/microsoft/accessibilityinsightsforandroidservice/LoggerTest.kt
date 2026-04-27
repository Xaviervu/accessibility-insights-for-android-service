// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.util.Log
import com.microsoft.accessibilityinsightsforandroidservice.Logger.logDebug
import com.microsoft.accessibilityinsightsforandroidservice.Logger.logError
import com.microsoft.accessibilityinsightsforandroidservice.Logger.logInfo
import com.microsoft.accessibilityinsightsforandroidservice.Logger.logVerbose
import com.microsoft.accessibilityinsightsforandroidservice.Logger.logWarning
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.MockedStatic.Verification
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoggerTest {
    val logTag: String = "logTag"
    val logMessage: String = "log message"

    var originalEnableLogging: Boolean = false
    var logStaticMock: MockedStatic<Log?>? = null

    @Before
    fun prepare() {
        logStaticMock = Mockito.mockStatic<Log?>(Log::class.java)
        originalEnableLogging = Logger.ENABLE_LOGGING
    }

    @After
    fun cleanUp() {
        Logger.ENABLE_LOGGING = originalEnableLogging
        logStaticMock!!.close()
    }

    @Test
    fun logVerboseDebugOn() {
        Logger.ENABLE_LOGGING = true

        logVerbose(logTag, logMessage)

        logStaticMock!!.verify(Verification { Log.v(logTag, logMessage) })
    }

    @Test
    fun logVerboseDebugOff() {
        Logger.ENABLE_LOGGING = false

        logVerbose(logTag, logMessage)

        logStaticMock!!.verifyNoMoreInteractions()
    }

    @Test
    fun logDebugDebugOn() {
        Logger.ENABLE_LOGGING = true

        logDebug(logTag, logMessage)

        logStaticMock!!.verify(Verification { Log.d(logTag, logMessage) })
    }

    @Test
    fun logDebugDebugOff() {
        Logger.ENABLE_LOGGING = false

        logDebug(logTag, logMessage)

        logStaticMock!!.verifyNoMoreInteractions()
    }

    @Test
    fun logErrorDebugOn() {
        Logger.ENABLE_LOGGING = true

        logError(logTag, logMessage)

        logStaticMock!!.verify(Verification { Log.e(logTag, logMessage) })
    }

    @Test
    fun logErrorDebugOff() {
        Logger.ENABLE_LOGGING = false

        logError(logTag, logMessage)

        logStaticMock!!.verifyNoMoreInteractions()
    }

    @Test
    fun logInfoDebugOn() {
        Logger.ENABLE_LOGGING = true

        logInfo(logTag, logMessage)

        logStaticMock!!.verify(Verification { Log.i(logTag, logMessage) })
    }

    @Test
    fun logInfoDebugOff() {
        Logger.ENABLE_LOGGING = false

        logInfo(logTag, logMessage)

        logStaticMock!!.verifyNoMoreInteractions()
    }

    @Test
    fun logWarningDebugOn() {
        Logger.ENABLE_LOGGING = true

        logWarning(logTag, logMessage)

        logStaticMock!!.verify(Verification { Log.w(logTag, logMessage) })
    }

    @Test
    fun logWarningDebugOff() {
        Logger.ENABLE_LOGGING = false

        logWarning(logTag, logMessage)

        logStaticMock!!.verifyNoMoreInteractions()
    }
}
