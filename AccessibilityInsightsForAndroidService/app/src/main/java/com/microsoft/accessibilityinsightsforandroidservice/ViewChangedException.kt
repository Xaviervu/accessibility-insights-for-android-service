// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

internal class ViewChangedException @JvmOverloads constructor(additionalMessage: String? = "") :
    Exception("The view hierarchy changed while building the AxeView tree. " + additionalMessage)
