/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.chunked

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpServerResponse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.*

class HttpChunkedJsonResponseTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val httpServerResponse = mock<HttpServerResponse>()

    @Test
    fun doEnd() {
        doReturn(null).`when`(httpServerResponse).end(anyString())

        // Check that ending the response sends the remaining buffer.
        HttpChunkedJsonResponse(httpServerResponse, ChunkedJsonResponse.DEFAULT_BUFFER_SIZE).ofArray { }
        verify(httpServerResponse).end("[]")
    }

    @Test
    fun doCheckWrite() {
        doReturn(null).`when`(httpServerResponse).write(anyString())

        // NOTE: Buffer needs to be big enough for the added escaping.
        HttpChunkedJsonResponse(httpServerResponse, 14).ofArray {
            //
            // NOTE: Every add of an item does a non-forced write check.
            //

            // Check that a non-forced send does nothing if the buffer is undersized.
            item("this")
            item("is")
            verify(httpServerResponse, never()).write(anyString())

            // Check that a forced send works even if the buffer is undersized.
            checkWrite(true)
            verify(httpServerResponse).write("[\"this\",\"is\"")

            // Check that the buffer has been reset and is again undersized.
            clearInvocations(httpServerResponse)
            item("my")
            verify(httpServerResponse, never()).write(anyString())

            // Check that a non-forced sends works once the buffer size is exceeded.
            item("test data")
            verify(httpServerResponse).write(",\"my\",\"test data\"")
        }
    }

    @Test
    fun responseCheck() {
        doReturn(true).`when`(httpServerResponse).closed()
        HttpChunkedJsonResponse(httpServerResponse, ChunkedJsonResponse.DEFAULT_BUFFER_SIZE).ofArray { }
        verify(httpServerResponse, never()).end(anyString())

        doReturn(false).`when`(httpServerResponse).closed()
        HttpChunkedJsonResponse(httpServerResponse, ChunkedJsonResponse.DEFAULT_BUFFER_SIZE).ofArray { }
        verify(httpServerResponse).end("[]")

        // Mark the response as closed to ensure the buffer isn't cleared when sending.
        doReturn(true).`when`(httpServerResponse).closed()
        doReturn(null).`when`(httpServerResponse).write(anyString())
        HttpChunkedJsonResponse(httpServerResponse, 10).ofArray {
            item("this")
            item("is")
            checkWrite(true)
            item("my")
            item("test data")

            verify(httpServerResponse, never()).write(anyString())

            // Mark the response as open to ensure the buffer is sent on array close.
            doReturn(false).`when`(httpServerResponse).closed()
        }

        verify(httpServerResponse).end("[\"this\",\"is\",\"my\",\"test data\"]")
    }

    @Test
    internal fun `can read and change status code`() {
        var statusCode = HttpResponseStatus.EARLY_HINTS.code()
        doAnswer { statusCode = it.getArgument(0); null }.`when`(httpServerResponse).statusCode = anyInt()
        doAnswer { statusCode }.`when`(httpServerResponse).statusCode

        val response = HttpChunkedJsonResponse(httpServerResponse)
        assertThat(response.statusCode, equalTo(HttpResponseStatus.EARLY_HINTS))
        assertThat(statusCode, equalTo(HttpResponseStatus.EARLY_HINTS.code()))

        fun validateChange(newStatus: HttpResponseStatus) {
            response.statusCode = newStatus
            // Did the mock get called with the correct value?
            assertThat(statusCode, equalTo(newStatus.code()))
            // Did we return the code for the value set?
            assertThat(response.statusCode, equalTo(newStatus))
        }

        validateChange(HttpResponseStatus.PROCESSING)
        validateChange(HttpResponseStatus.OK)
    }

    @Test
    internal fun `can't change status code after the response has been committed via write`() {
        // Ensure once a response is committed (first write or end), it is too late to change the status.
        val response = HttpChunkedJsonResponse(httpServerResponse)
        response.ofObject {
            // Should be able to set it before we send anything
            response.statusCode = HttpResponseStatus.OK

            checkWrite(force = true)

            // Now we have committed the message via a `write`, we shouldn't be able to change the status.
            expect { response.statusCode = HttpResponseStatus.NOT_FOUND }
                .toThrow<IllegalStateException>()
                .withMessage("You can't set the status after the response has been committed")
        }
    }

    @Test
    internal fun `can't change status code after the response has been committed via end`() {
        // Ensure once a response is committed (first write or end), it is too late to change the status.
        val response = HttpChunkedJsonResponse(httpServerResponse)

        // Should be able to set it before we send anything
        response.statusCode = HttpResponseStatus.OK

        response.ofObject {}

        // Now we have committed the message via ending the response, we shouldn't be able to change the status.
        expect { response.statusCode = HttpResponseStatus.NOT_FOUND }
            .toThrow<IllegalStateException>()
            .withMessage("You can't set the status after the response has been committed")
    }

}
