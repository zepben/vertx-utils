/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.ErrorFormatter
import com.zepben.vertxutils.routing.Respond
import com.zepben.vertxutils.routing.handlers.UtilHandlers.CATCH_ALL_API_FAILURE_HANDLER
import com.zepben.vertxutils.routing.handlers.UtilHandlers.REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER
import io.mockk.*
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.VertxException
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.nio.channels.ClosedChannelException

class UtilHandlersTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()
    }

    private var failure: Throwable = RuntimeException("test")
    private val response = mockk<HttpServerResponse> {
        every { ended() } returns false
    }.also {
        every { it.setStatusCode(any()) } returns it
        every { it.setStatusMessage(any()) } returns it
        every { it.putHeader(any<String>(), any<String>()) } returns it
        every { it.setStatusCode(any()) } returns mockk()
    }
    private val context = mockk<RoutingContext> {
        every { response() } returns response
        every { statusCode() } returns -1
        every { failure() } answers { failure }
    }

    @BeforeEach
    fun setUp() {
        mockkObject(Respond, ErrorFormatter)

        justRun { Respond.withJson(any(), any(), any()) }
        justRun { Respond.with(any(), any<HttpResponseStatus>()) }

        every { ErrorFormatter.asJson(any<String>()) } returns "formatted error"
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(Respond, ErrorFormatter)
    }

    //                Respond.withJson(
    //                    context,
    //                    context.statusCode().takeUnless { it == -1 }?.let { HttpResponseStatus.valueOf(it) }
    //                        ?: HttpResponseStatus.INTERNAL_SERVER_ERROR,
    //                    ErrorFormatter.ErrorFormatter.asJson(failure.toString()),
    //                )

    @Test
    fun `failure handler uses 500 by default`() {
        CATCH_ALL_API_FAILURE_HANDLER.handle(context)

        verifySequence {
            // Calls when checking the context failure.
            context.failure()
            context.response()
            response.ended()

            // Calls when sending the response.
            context.statusCode()
            ErrorFormatter.asJson(failure.toString())
            Respond.withJson(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, "formatted error")
        }
    }

    @Test
    fun `failure handler uses context status when available`() {
        every { context.statusCode() } returns HttpResponseStatus.UNAUTHORIZED.code()

        CATCH_ALL_API_FAILURE_HANDLER.handle(context)

        verifySequence {
            // Calls when checking the context failure.
            context.failure()
            context.response()
            response.ended()

            // Calls when sending the response.
            context.statusCode()
            ErrorFormatter.asJson(failure.toString())
            Respond.withJson(context, HttpResponseStatus.UNAUTHORIZED, "formatted error")
        }
    }

    @Test
    internal fun `failure handler ignores vertx closed channel exceptions`() {
        every { response.ended() } returns true
        failure = VertxException("Connection was closed")

        CATCH_ALL_API_FAILURE_HANDLER.handle(context)

        verifySequence {
            // Calls when checking the context failure.
            context.failure()
            context.response()
            response.ended()
        }

        // Should be no response.
        confirmVerified(ErrorFormatter, Respond)
    }

    @Test
    internal fun `failure handler ignores java closed channel exceptions`() {
        every { response.ended() } returns true
        failure = ClosedChannelException()

        CATCH_ALL_API_FAILURE_HANDLER.handle(context)

        verifySequence {
            // Calls when checking the context failure.
            context.failure()
            context.response()
            response.ended()
        }

        // Should be no response.
        confirmVerified(ErrorFormatter, Respond)
    }

    @Test
    internal fun `failure handler calls default handler if not processed`() {
        every { response.ended() } returns true
        justRun { context.next() }

        CATCH_ALL_API_FAILURE_HANDLER.handle(context)

        verifySequence {
            // Calls when checking the context failure.
            context.failure()
            context.response()
            response.ended()

            // Should move on to the next handler as we didn't handle it.
            context.next()
        }

        // Should be no response.
        confirmVerified(ErrorFormatter, Respond)
    }

    @Test
    fun `redirects no trailing to trailing`() {
        val request = mockk<HttpServerRequest> {
            every { path() } returns "/some/path/without/slash"
            every { query() } returns null
        }.also {
            every { context.request() } returns it
        }

        REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER.handle(context)

        verifySequence {
            context.request()
            request.path()

            // Query is read once if it has no value.
            context.request()
            request.query()

            // Configure and send the redirect.
            context.response()
            response.putHeader("Location", "/some/path/without/slash/")
            Respond.with(context, HttpResponseStatus.MOVED_PERMANENTLY)
        }
    }

    @Test
    fun `redirects no trailing to trailing with query params`() {
        val request = mockk<HttpServerRequest> {
            every { path() } returns "/some/path/without/slash"
            every { query() } returns "test=true"
        }.also {
            every { context.request() } returns it
        }

        REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER.handle(context)

        verifySequence {
            context.request()
            request.path()

            // Query is read twice if it has a value.
            context.request()
            request.query()
            context.request()
            request.query()

            // Configure and send the redirect.
            context.response()
            response.putHeader("Location", "/some/path/without/slash/?test=true")
            Respond.with(context, HttpResponseStatus.MOVED_PERMANENTLY)
        }
    }

}
