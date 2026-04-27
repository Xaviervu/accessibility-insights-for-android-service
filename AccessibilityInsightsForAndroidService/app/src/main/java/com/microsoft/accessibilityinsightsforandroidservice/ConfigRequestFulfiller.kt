// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import android.os.CancellationSignal

class ConfigRequestFulfiller(
    private val rootNodeFinder: RootNodeFinder,
    private val eventHelper: EventHelper,
    private val deviceConfigFactory: DeviceConfigFactory
) : RequestFulfiller {
    override fun fulfillRequest(cancellationSignal: CancellationSignal): String {
        val source = eventHelper.claimLastSource()
        val rootNode = rootNodeFinder.getRootNodeFromSource(source)

        try {
            return deviceConfigFactory.getDeviceConfig(rootNode).toJson()
        } finally {
            if (rootNode != null && rootNode !== source) {
                rootNode.recycle()
            }
            if (source != null && !eventHelper.restoreLastSource(source)) {
                source.recycle()
            }
        }
    }
}
