/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.chunked

class CaptureChunkedJsonResponse(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : ChunkedJsonResponse(bufferSize) {

    private var captured = ""

    override fun onResponseCompleted(sb: StringBuilder) {
        captured = sb.toString()
    }

    override fun checkWrite(sb: StringBuilder, force: Boolean) {
        captured = sb.toString()
    }

    override fun toString(): String {
        return captured
    }

}
