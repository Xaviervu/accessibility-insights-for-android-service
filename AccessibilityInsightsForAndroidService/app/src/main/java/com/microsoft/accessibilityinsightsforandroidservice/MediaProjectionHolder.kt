// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import android.media.projection.MediaProjection

object MediaProjectionHolder {
    private var sharedMediaProjection: MediaProjection? = null

    fun cleanUp() {
        sharedMediaProjection?.stop()
        sharedMediaProjection = null
    }

    @JvmStatic
    fun get(): MediaProjection? = sharedMediaProjection

    @JvmStatic
    fun set(projection: MediaProjection?) {
        sharedMediaProjection = projection
    }
}
