// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice

import com.deque.axe.android.utils.JsonSerializable

class DeviceConfig(
    @JvmField val deviceName: String?,
    @JvmField val packageName: String?,
    @JvmField val serviceVersion: String?,
) : JsonSerializable
