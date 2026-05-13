/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.testing

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.Route.Companion.builder
import io.restassured.RestAssured
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestHttpServerTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun canServe() {
        TestHttpServer().use { server ->
            val route = builder().path("/").addHandler { ctx -> ctx!!.response().end("The response!") }.build()
            val port = server.addRoute(route).listen()
            RestAssured.given()
                .port(port)
                .get("/")
                .then()
                .statusCode(200)
                .body(equalTo("The response!"))
        }
    }

}
