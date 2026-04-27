// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class RequestReaderFactory {
    fun createRequestReader(inputStream: InputStream?): RequestReader {
        return RequestReader(BufferedReader(InputStreamReader(inputStream)))
    }
}
