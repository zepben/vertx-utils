/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class FaviconHandlerTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @TempDir
    lateinit var temporaryFolder: File

    private val vertx: Vertx = Vertx.vertx()
    private val buffer = Buffer.buffer("the icon!")
    private val response = mock<HttpServerResponse>()
    private val context = mock<RoutingContext>().also {
        doReturn(response).`when`(it).response()
        doReturn(vertx).`when`(it).vertx()
    }

    private val handler: FaviconHandler by lazy {
        val faviconPath = Paths.get(temporaryFolder.path, "favicon.ico")
        Files.write(faviconPath, buffer.bytes)
        FaviconHandler(faviconPath.toString(), 10)
    }

    @AfterEach
    fun tearDown() {
        vertx.close()
    }

    @Test
    fun handle() {
        handler.handle(context)

        verify(response).putHeader("Content-Type", "image/x-icon")
        verify(response).putHeader("Content-Length", buffer.length().toString())
        verify(response).putHeader("Cache-Control", "public, max-age=" + 10)
        verify(response).end(buffer)
    }

    @Test
    fun cachesIcon() {
        handler.handle(context)
        Files.delete(Paths.get(handler.faviconPath()))

        // Previously you got really strange error messages if this was wrong, so make it throw an exception with a useful message.
        doThrow(IllegalStateException("should have been cached and not called `setStatusCode`"))
            .`when`(response).statusCode = anyInt()

        handler.handle(context)
        verify(response, times(2)).end(buffer)
    }

    @Test
    fun maxAgeMustBePositive() {
        expect { FaviconHandler(handler.faviconPath(), -1) }.toThrow<IllegalArgumentException>()
    }

}
