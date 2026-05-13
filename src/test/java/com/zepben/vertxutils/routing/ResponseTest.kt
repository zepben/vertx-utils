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
import com.zepben.testutils.junit.SystemLogExtension
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.buffer.Buffer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ResponseTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun statusConstructor() {
        val response = Response(HttpResponseStatus.OK, Buffer.buffer())

        assertThat(response.status, equalTo(HttpResponseStatus.OK))
        assertThat(response.body, equalTo(Buffer.buffer()))
    }

    @Test
    fun statusBodyConstructor() {
        val buffer = Buffer.buffer("test")
        val response = Response(HttpResponseStatus.OK, buffer)

        assertThat(response.status, equalTo(HttpResponseStatus.OK))
        assertThat(response.body, equalTo(buffer))
    }

    @Test
    fun ofJson() {
        val response = Response.ofJson(HttpResponseStatus.OK, "json")

        assertThat(response.status, equalTo(HttpResponseStatus.OK))
        assertThat(response.body, equalTo(Buffer.buffer("json")))
        assertThat(response.headers[HttpHeaders.CONTENT_TYPE], equalTo(MediaType.JSON_UTF_8.toString()))
    }

    @Test
    fun ofText() {
        val response = Response.ofText(HttpResponseStatus.OK, "text")

        assertThat(response.status, equalTo(HttpResponseStatus.OK))
        assertThat(response.body, equalTo(Buffer.buffer("text")))
        assertThat(response.headers[HttpHeaders.CONTENT_TYPE], equalTo(MediaType.PLAIN_TEXT_UTF_8.toString()))
    }

}
