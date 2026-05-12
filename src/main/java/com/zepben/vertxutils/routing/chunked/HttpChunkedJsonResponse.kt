/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.chunked

import io.vertx.core.http.HttpServerResponse

class HttpChunkedJsonResponse(
    private val response: HttpServerResponse,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : ChunkedJsonResponse(bufferSize) {

    override fun onResponseCompleted(sb: StringBuilder) {
        if (!response.closed()) {
            response.end(sb.toString())
        }
    }

    override fun checkWrite(sb: StringBuilder, force: Boolean) {
        if ((force || (sb.length >= bufferSize)) && !response.closed()) {
            response.write(sb.toString())
            sb.setLength(0)
        }
    }

}
