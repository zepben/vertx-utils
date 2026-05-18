/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.Route.Companion.builder
import io.vertx.core.http.HttpMethod
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.*
import org.slf4j.Logger

class RouteRegisterLoggerTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun logsRouteDetails() {
        val logger = mock<Logger>()

        val paths = arrayOf("/my/path/1", "/my/path/2", "/my/other/3")
        val methods = arrayOf(
            arrayOf(HttpMethod.GET),
            arrayOf(HttpMethod.PUT),
            arrayOf(HttpMethod.GET, HttpMethod.PUT),
        )
        assertThat(paths.size, equalTo(methods.size))

        val routeRegisterLogger = logRegisteredRoutes(logger)
        paths.forEachIndexed { i, path ->
            routeRegisterLogger.invoke("/mount$path", builder().path(path).methods(*methods[i]).build())

            methods[i].forEach { method ->
                verify(logger, times(1)).info("$method: /mount$path")
            }
        }
    }

}
