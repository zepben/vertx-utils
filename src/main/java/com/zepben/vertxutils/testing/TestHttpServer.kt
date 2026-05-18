/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.testing

import com.zepben.vertxutils.routing.Route
import com.zepben.vertxutils.routing.RouteRegister
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import java.io.IOException
import java.io.UncheckedIOException
import java.lang.AutoCloseable
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch

class TestHttpServer(
    orderBlockingRules: Boolean = true,
) : AutoCloseable {

    private val vertx: Vertx = Vertx.vertx()
    private val server: HttpServer = vertx.createHttpServer()
    private val router: Router = Router.router(vertx)
    private val routeRegister: RouteRegister = RouteRegister(router, defaultOrderedBlockingRoutes = orderBlockingRules)

    fun addRoute(route: Route): TestHttpServer = apply {
        routeRegister.add(route)
    }

    fun addRoutes(routes: Iterable<Route>): TestHttpServer = apply {
        routeRegister.add(routes)
    }

    fun listen(): Int {
        val latch = CountDownLatch(1)
        server.requestHandler(router)
            .listen(
                this.randomPortNumber,
            ) { res ->
                if (res.failed()) throw RuntimeException(res.cause())
                latch.countDown()
            }

        try {
            latch.await()
        } catch (_: InterruptedException) {
        }

        return server.actualPort()
    }

    override fun close() {
        val latch = CountDownLatch(2)
        server.close { latch.countDown() }
        vertx.close { latch.countDown() }

        try {
            latch.await()
        } catch (ex: InterruptedException) {
            throw RuntimeException(ex)
        }
    }

    private val randomPortNumber: Int
        get() =
            try {
                ServerSocket(0).use { it.getLocalPort() }
            } catch (ex: IOException) {
                throw UncheckedIOException(ex)
            }

}
