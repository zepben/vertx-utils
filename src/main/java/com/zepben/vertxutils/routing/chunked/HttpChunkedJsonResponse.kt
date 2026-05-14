/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.chunked

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpServerResponse

class HttpChunkedJsonResponse(
    private val response: HttpServerResponse,
    private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
) : ChunkedJsonResponse(bufferSize) {

    private var canSetStatus = true

    override fun onResponseCompleted(sb: StringBuilder) {
        canSetStatus = false
        if (!response.closed()) {
            response.end(sb.toString())
        }
    }

    override fun checkWrite(sb: StringBuilder, force: Boolean) {
        canSetStatus = false
        if ((force || (sb.length >= bufferSize)) && !response.closed()) {
            response.write(sb.toString())
            sb.setLength(0)
        }
    }

    var statusCode: HttpResponseStatus
        get() = HttpResponseStatus.valueOf(response.statusCode)
        set(status) {
            // Once a response is committed (first write or end), it is too late to change the status.
            check(canSetStatus) { "You can't set the status after the response has been committed" }
            response.statusCode = status.code()
        }

}
