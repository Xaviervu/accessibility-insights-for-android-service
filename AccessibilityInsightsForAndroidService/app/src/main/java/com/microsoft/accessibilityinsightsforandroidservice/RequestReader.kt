// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.
package com.microsoft.accessibilityinsightsforandroidservice

import java.io.BufferedReader
import java.io.IOException

class RequestReader(private val reader: BufferedReader) {
    @Throws(IOException::class)
    fun readRequest(): String {
        val buffer = StringBuffer()
        var intC = reader.read()
        while (intC != -1) {
            val c = intC.toChar()
            if (c == '\n') {
                break
            }
            if (buffer.length >= maxLineLength) {
                throw IOException("input too long")
            }
            buffer.append(c)
            intC = reader.read()
        }

        return buffer.toString()
    }

    companion object {
        private const val maxLineLength = 256
    }
}
