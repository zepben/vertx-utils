/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.testutils.junit.SystemLogExtension
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import java.io.IOException

class ExceptionHandlerTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val context = mock<RoutingContext>()

    @Test
    fun handlesExceptions() {
        val ioEx = IOException("test")
        val ioExHandler = mock<(IOException, RoutingContext?) -> Unit>()
        doReturn(ioEx).`when`(context).failure()

        val handler = ExceptionHandler.of(ioExHandler)
        handler.handle(context)

        verify(ioExHandler).invoke(ioEx, context)
        verify(context, never()).next()
    }

    @Test
    fun handlesNoFailure() {
        ExceptionHandler.of<RuntimeException> { _, _ -> }.handle(context)
        verify(context).next()
    }

    @Test
    fun handlesNoMatch() {
        doReturn(RuntimeException()).`when`(context).failure()
        val ioExHandler = mock<(IOException, RoutingContext?) -> Unit>()

        val handler = ExceptionHandler.of(ioExHandler)
        handler.handle(context)

        verify(ioExHandler, never()).invoke(any(), any())
        verify(context).next()
    }

}
