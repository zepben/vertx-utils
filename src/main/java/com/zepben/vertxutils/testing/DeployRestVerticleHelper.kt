/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.testing

import com.jayway.awaitility.Awaitility
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import io.vertx.core.DeploymentOptions
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import java.io.IOException
import java.lang.AutoCloseable
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class DeployRestVerticleHelper(
    verticleClass: Class<*>,
    config: JsonObject,
) : AutoCloseable {

    val requestSpec: RequestSpecification
    private val vertx: Vertx

    init {
        try {
            val port = this.randomPortNumber
            config.put("http.port", port)

            // Start the server
            val promise = Promise.promise<Void>()
            val future = promise.future()
            vertx = Vertx.vertx()
            val options = DeploymentOptions().setConfig(config)
            vertx.deployVerticle(
                verticleClass.getName(),
                options,
            ) { ar ->
                if (ar!!.succeeded()) promise.complete()
                else promise.fail(ar.cause())
            }

            Awaitility.await().atMost(5, TimeUnit.SECONDS).until { future.isComplete }

            if (!future.succeeded()) throw AssertionError(future.cause().message)

            requestSpec = RequestSpecBuilder().setBaseUri("http://localhost").setPort(port).build()
        } catch (ex: IOException) {
            throw AssertionError("Failed to start server", ex)
        }
    }

    override fun close() {
        val done = AtomicBoolean(false)
        vertx.close { done.set(true) }
        Awaitility.await().until { done.get() }
    }

    @get:Throws(IOException::class)
    val randomPortNumber: Int
        get() = ServerSocket(0).use { it.getLocalPort() }

}
