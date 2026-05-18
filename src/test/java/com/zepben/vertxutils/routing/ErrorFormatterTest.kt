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
import com.zepben.vertxutils.routing.ErrorFormatter.asJson
import com.zepben.vertxutils.routing.handlers.UtilHandlers.CATCH_ALL_API_FAILURE_HANDLER
import com.zepben.vertxutils.routing.handlers.UtilHandlers.REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.*

class ErrorFormatterTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun defaultFailureHandler() {
        val context = mock<RoutingContext>()
        val response = mock<HttpServerResponse>(RETURNS_SELF)
        doReturn(response).`when`(context).response()

        val failure: Throwable = RuntimeException("test")
        doReturn(failure).`when`(context).failure()
        CATCH_ALL_API_FAILURE_HANDLER.handle(context)

        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        verify(response).end(asJson(failure.toString()))
    }

    @Test
    fun redirectNoTrailingToTrailing() {
        val context = mock(RoutingContext::class.java)
        val request = mock<HttpServerRequest>()
        val response = mock<HttpServerResponse>(RETURNS_SELF)
        doReturn(request).`when`(context).request()
        doReturn(response).`when`(context).response()
        doReturn("/some/path/without/slash").`when`(request).path()
        doReturn("test=true").`when`(request).query()

        REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER.handle(context)

        verify(response).putHeader("Location", "/some/path/without/slash/?test=true")
        verify(response).statusCode = 301
        verify(response).end()
    }

    @Test
    fun errorToJson() {
        val err = "err"
        val actual = asJson(err)
        val expected = JsonObject().put("errors", listOf(err)).encode()
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun errorsToJson() {
        val errs = listOf("err1", "err2")
        val actual = asJson(errs)
        val expected = JsonObject().put("errors", errs).encode()
        assertThat(actual, equalTo(expected))
    }

}
