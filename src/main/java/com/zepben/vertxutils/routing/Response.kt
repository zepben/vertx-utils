/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.buffer.Buffer

/**
 * Class to hold things for a response to an HTTP request.
 */
class Response(
    val status: HttpResponseStatus,
    val body: Buffer = Buffer.buffer(),
    val headers: Map<String, String> = emptyMap(),
) {

    fun hasHeaders(): Boolean = !headers.isEmpty()

    companion object {

        fun ofJson(status: HttpResponseStatus, json: String): Response =
            Response(
                status,
                Buffer.buffer(json),
                mapOf(HttpHeaders.CONTENT_TYPE to MediaType.JSON_UTF_8.toString()),
            )

        fun ofText(status: HttpResponseStatus, body: String): Response =
            Response(
                status,
                Buffer.buffer(body),
                mapOf(HttpHeaders.CONTENT_TYPE to MediaType.PLAIN_TEXT_UTF_8.toString()),
            )

    }

}
