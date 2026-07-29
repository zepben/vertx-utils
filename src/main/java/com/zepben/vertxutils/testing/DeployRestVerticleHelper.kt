/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.testing

import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import java.io.IOException
import java.lang.AutoCloseable
import java.net.ServerSocket
import java.util.concurrent.TimeUnit

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
            vertx = Vertx.vertx()
            runCatching {
                vertx.deployVerticle(verticleClass.getName(), DeploymentOptions().setConfig(config))
                    .await(5, TimeUnit.SECONDS)
            }.onFailure {
                // Catch any exception raised and convert it to an `AssertionError` for the testing framework.
                throw AssertionError(it.message, it)
            }

            requestSpec = RequestSpecBuilder().setBaseUri("http://localhost").setPort(port).build()
        } catch (ex: IOException) {
            throw AssertionError("Failed to start server", ex)
        }
    }

    override fun close() {
        vertx.close().await()
    }

    @get:Throws(IOException::class)
    val randomPortNumber: Int
        get() = ServerSocket(0).use { it.getLocalPort() }

}
