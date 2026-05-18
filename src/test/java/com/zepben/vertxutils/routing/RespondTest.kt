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
import com.zepben.vertxutils.json.filter.FilterSpecification
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.MultiMap
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.*

class RespondTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val response = mock<HttpServerResponse>(RETURNS_SELF)
    private val context = mock<RoutingContext>().also {
        doReturn(response).`when`(it).response()
    }

    @Test
    fun withStatus() {
        Respond.with(context, HttpResponseStatus.OK)
        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(response).end()
    }

    @Test
    fun withStatusPlusHeaders() {
        val existingHeaders = mock(MultiMap::class.java)
        val addHeaders = mapOf("X-Test-Header" to "value", "X-Test-Header-2" to "value2")
        doReturn(existingHeaders).`when`(this.response).headers()

        Respond.with(context, HttpResponseStatus.OK, addHeaders)

        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(existingHeaders).addAll(addHeaders)
        verify(response).end()
    }

    @Test
    fun withStatusPlusEmptyContentLengthHeader() {
        val existingHeaders = mock(MultiMap::class.java)
        doReturn(existingHeaders).`when`(this.response).headers()

        Respond.with(context, HttpResponseStatus.OK, withEmptyContentLengthHeader = true)

        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(existingHeaders).set(HttpHeaders.CONTENT_LENGTH, "0")
        verify(response).end()
    }

    @Test
    fun withResponse() {
        val response = Response.ofText(HttpResponseStatus.OK, "test")
        val headers = mock(MultiMap::class.java)
        doReturn(headers).`when`(this.response).headers()

        Respond.with(context, response)

        verify(this.response).statusCode = HttpResponseStatus.OK.code()
        verify(headers).addAll(response.headers)
        verify(this.response).end(response.body)
    }

    @Test
    fun withJson() {
        Respond.withJson(context, HttpResponseStatus.OK, "json")

        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        verify(response).end("json")
    }

    @Test
    fun withJsonPlusHeaders() {
        val existingHeaders = mock(MultiMap::class.java)
        doReturn(existingHeaders).`when`(this.response).headers()
        val addHeaders = mapOf("X-Test-Header" to "value", "X-Test-Header-2" to "value2")

        Respond.withJson(context, HttpResponseStatus.OK, "json", addHeaders)

        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        verify(existingHeaders).addAll(addHeaders)
        verify(response).end("json")
    }

    @Test
    fun withJsonFilter() {
        val filterSpecification = FilterSpecification("a.b")

        val jsonObject = JsonObject()
            .put(
                "a",
                JsonObject()
                    .put("b", 1)
                    .put("c", 2),
            )
            .put("d", 3)

        Respond.withJson(context, HttpResponseStatus.OK, jsonObject, filterSpecification)

        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        verify(response).end("{\"a\":{\"b\":1}}")
    }

    @Test
    fun withJsonFilterPlusHeaders() {
        val filterSpecification = FilterSpecification("a.b")

        val jsonObject = JsonObject()
            .put(
                "a",
                JsonObject()
                    .put("b", 1)
                    .put("c", 2),
            )
            .put("d", 3)

        val existingHeaders = mock(MultiMap::class.java)
        doReturn(existingHeaders).`when`(this.response).headers()
        val addHeaders = mapOf("X-Test-Header" to "value", "X-Test-Header-2" to "value2")

        Respond.withJson(context, HttpResponseStatus.OK, jsonObject, filterSpecification, addHeaders)

        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        verify(existingHeaders).addAll(addHeaders)
        verify(response).end("{\"a\":{\"b\":1}}")
    }

    @Test
    fun withJsonChunked() {
        val returnedResponse = Respond.withJsonChunked(context, HttpResponseStatus.OK)

        assertThat(returnedResponse, equalTo(response))

        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        verify(response).isChunked = true
    }

    @Test
    fun withJsonChunkedPlusHeaders() {
        val existingHeaders = mock(MultiMap::class.java)
        doReturn(existingHeaders).`when`(this.response).headers()
        val addHeaders = mapOf("X-Test-Header" to "value", "X-Test-Header-2" to "value2")

        val returnedResponse = Respond.withJsonChunked(context, HttpResponseStatus.OK, addHeaders)

        assertThat(returnedResponse, equalTo(response))

        verify(response).statusCode = HttpResponseStatus.OK.code()
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        verify(existingHeaders).addAll(addHeaders)
        verify(response).isChunked = true
    }

}
