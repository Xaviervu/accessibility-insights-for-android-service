// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import com.microsoft.accessibilityinsightsforandroidservice.MediaProjectionHolder.get
import com.microsoft.accessibilityinsightsforandroidservice.MediaProjectionHolder.set

class ScreenshotActivity : Activity() {
    private lateinit var mediaManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaManager.createScreenCaptureIntent(), SCREENSHOT)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        if (requestCode == SCREENSHOT) {
            if (resultCode == RESULT_OK && data != null) {
                set(mediaManager.getMediaProjection(resultCode, data))
            }
        }

        if (get() == null) {
            Toast
                .makeText(this, R.string.screenshot_permission_not_granted, Toast.LENGTH_LONG)
                .show()
        }

        finish()
    }

    companion object {
        private const val SCREENSHOT = 99999
    }
}
