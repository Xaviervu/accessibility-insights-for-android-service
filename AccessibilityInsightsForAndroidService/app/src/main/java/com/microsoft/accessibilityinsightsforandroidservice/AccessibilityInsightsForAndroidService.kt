// Portions Copyright (c) Microsoft Corporation
// Licensed under the MIT License.
//
// Copyright 2016 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.google.gson.GsonBuilder
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAResultsSerializer
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFARulesSerializer
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAScanner
import com.microsoft.accessibilityinsightsforandroidservice.atfa.ATFAScannerFactory
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeScanner
import com.microsoft.accessibilityinsightsforandroidservice.axe.AxeScannerFactory
import java.util.function.Consumer

@SuppressLint("AccessibilityPolicy")
class AccessibilityInsightsForAndroidService : AccessibilityService() {
    private val axeScanner: AxeScanner
    private val atfaScanner: ATFAScanner
    private val eventHelper: EventHelper
    private val deviceConfigFactory: DeviceConfigFactory = DeviceConfigFactory()
    private val onScreenshotAvailableProvider = OnScreenshotAvailableProvider()
    private val bitmapProvider = BitmapProvider()
    private var screenshotHandlerThread: HandlerThread? = null
    private var screenshotController: ScreenshotController? = null
    private var activeWindowId = -1 // Set initial state to an invalid ID
    private lateinit var focusVisualizationStateManager: FocusVisualizationStateManager
    private lateinit var focusVisualizer: FocusVisualizer
    private lateinit var focusVisualizerController: FocusVisualizerController
    private lateinit var focusVisualizationCanvas: FocusVisualizationCanvas
    private lateinit var accessibilityEventDispatcher: AccessibilityEventDispatcher
    private lateinit var deviceOrientationHandler: DeviceOrientationHandler
    private lateinit var tempFileProvider: TempFileProvider

    init {
        axeScanner =
            AxeScannerFactory.createAxeScanner(
                deviceConfigFactory
            ) { this.realDisplayMetrics }
        atfaScanner = ATFAScannerFactory.createATFAScanner(this)
        eventHelper = EventHelper(ThreadSafeSwapper<AccessibilityNodeInfo?>())
    }

    private val realDisplayMetrics: DisplayMetrics
        get() =// Correct screen metrics are only accessible within the context of the running
            // service. They're not available when the service initializes, hence the callback
            DisplayMetricsHelper.getRealDisplayMetrics(this)

    private fun stopScreenshotHandlerThread() {
        if (screenshotHandlerThread != null) {
            screenshotHandlerThread!!.quit()
            screenshotHandlerThread = null
        }

        screenshotController = null
    }

    override fun onServiceConnected() {
        Logger.logVerbose(TAG, "*** onServiceConnected")

        this.startForegroundService()
        this.startScreenshotActivity()

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK
        info.notificationTimeout = 0
        info.flags =
            (AccessibilityServiceInfo.DEFAULT
                    or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS)

        setServiceInfo(info)

        stopScreenshotHandlerThread()
        screenshotHandlerThread = HandlerThread("ScreenshotHandlerThread")
        screenshotHandlerThread!!.start()
        val screenshotHandler = Handler(screenshotHandlerThread!!.getLooper())

        screenshotController =
            ScreenshotController(
                { this.realDisplayMetrics },
                screenshotHandler,
                onScreenshotAvailableProvider,
                bitmapProvider,
                { MediaProjectionHolder.get() })

        SynchronizedRequestDispatcher.SharedInstance.teardown()
        tempFileProvider = TempFileProvider(applicationContext)
        tempFileProvider.cleanOldFilesBestEffort()

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        focusVisualizationStateManager = FocusVisualizationStateManager()
        val layoutParamGenerator = LayoutParamGenerator { this.realDisplayMetrics }
        focusVisualizationCanvas = FocusVisualizationCanvas(this)
        focusVisualizer = FocusVisualizer(FocusVisualizerStyles(), focusVisualizationCanvas)
        focusVisualizerController = FocusVisualizerController(
            focusVisualizer,
            focusVisualizationStateManager,
            UIThreadRunner(),
            windowManager,
            layoutParamGenerator,
            focusVisualizationCanvas,
            DateProvider()
        )
        accessibilityEventDispatcher = AccessibilityEventDispatcher()
        deviceOrientationHandler =
            DeviceOrientationHandler(resources.configuration.orientation)
        val rootNodeFinder = RootNodeFinder()
        val resultsV2ContainerSerializer = ResultsV2ContainerSerializer(
            ATFARulesSerializer(),
            ATFAResultsSerializer(GsonBuilder()),
            GsonBuilder()
        )

        setupFocusVisualizationListeners()

        val requestDispatcher = RequestDispatcher(
            rootNodeFinder,
            screenshotController!!,
            eventHelper,
            axeScanner,
            atfaScanner,
            deviceConfigFactory,
            focusVisualizationStateManager,
            resultsV2ContainerSerializer
        )
        SynchronizedRequestDispatcher.SharedInstance.setup(requestDispatcher)
    }

    private fun startForegroundService() {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.accessibility_service_label),
                    NotificationManager.IMPORTANCE_LOW
                )
            notificationManager.createNotificationChannel(channel)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        val notification =
            builder
                .setContentTitle(getString(R.string.accessibility_service_label))
                .setSmallIcon(R.mipmap.blue_launcher)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun setupFocusVisualizationListeners() {
        accessibilityEventDispatcher.addOnRedrawEventListener(Consumer { event: AccessibilityEvent ->
            focusVisualizerController.onRedrawEvent(
                event
            )
        })
        accessibilityEventDispatcher.addOnFocusEventListener(Consumer { event: AccessibilityEvent ->
            focusVisualizerController.onFocusEvent(
                event
            )
        })
        accessibilityEventDispatcher.addOnAppChangedListener(Consumer { nodeInfo: AccessibilityNodeInfo ->
            focusVisualizerController.onAppChanged(
                nodeInfo
            )
        })
        deviceOrientationHandler.subscribe { orientation: Int? ->
            focusVisualizerController.onOrientationChanged(
                orientation
            )
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.logVerbose(TAG, "*** onUnbind")
        SynchronizedRequestDispatcher.SharedInstance.teardown()
        tempFileProvider.cleanOldFilesBestEffort()
        stopScreenshotHandlerThread()
        MediaProjectionHolder.cleanUp()
        stopForeground(true)
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        accessibilityEventDispatcher.onAccessibilityEvent(event, rootInActiveWindow)

        // This logic ensures that we only track events from the active window, as
        // described under "Retrieving window content" of the Android service docs at
        // https://www.android-doc.com/reference/android/accessibilityservice/AccessibilityService.html
        val windowId = event.windowId

        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER || eventType == AccessibilityEvent.TYPE_VIEW_HOVER_EXIT) {
            activeWindowId = windowId
        }

        if (activeWindowId == windowId) {
            eventHelper.recordEvent(rootInActiveWindow)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        this.deviceOrientationHandler.setOrientation(newConfig.orientation)
    }

    override fun onInterrupt() {}

    private fun startScreenshotActivity() {
        val startScreenshot = Intent(this, ScreenshotActivity::class.java)
        startScreenshot.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(startScreenshot)
    }

    companion object {
        private const val TAG = "AccessibilityInsightsForAndroidService"
        private const val NOTIFICATION_CHANNEL_ID = "accessibility_insights_service"
        private const val NOTIFICATION_ID = 1
    }
}
