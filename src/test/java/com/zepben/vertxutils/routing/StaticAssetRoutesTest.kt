/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.testutils.junit.SystemLogExtension
import io.restassured.RestAssured
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch

class StaticAssetRoutesTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @TempDir
    lateinit var temporaryFolder: File

    private val baseUrl = "/app"
    private val port = 34251.also { RestAssured.port = it }
    private val vertx: Vertx = Vertx.vertx()

    private val filePath: String by lazy { Files.createDirectory(Paths.get(temporaryFolder.toString(), "webroot")).toString() }
    private val staticRoutes: StaticAssetRoutes by lazy { StaticAssetRoutes(baseUrl, filePath) }

    @AfterEach
    fun tearDown() {
        val latch = CountDownLatch(1)
        vertx.close { latch.countDown() }
        latch.await()
    }

    @Test
    fun indexPage() {
        val html = "<html>The page</html>"
        Files.write(Paths.get(filePath, "index.html"), html.toByteArray(StandardCharsets.UTF_8))

        staticRoutes.indexPage("index.html")
        val routes = staticRoutes.buildRoutes()
        assertThat(routes.size, equalTo(2))
        startServer(routes)

        RestAssured.given()
            .`when`()
            .redirects()
            .follow(false)
            .get(baseUrl)
            .then()
            .statusCode(301)
            .header("Location", "$baseUrl/")

        RestAssured.given()
            .`when`()
            .get("$baseUrl/")
            .then()
            .statusCode(200)
            .header("Content-Type", "text/html;charset=UTF-8")
            .body(equalTo(html))
    }

    @Test
    fun favicon() {
        val ico = "This should be an image!"
        Files.write(Paths.get(filePath, "favicon.ico"), ico.toByteArray(StandardCharsets.UTF_8))

        staticRoutes.favicon("favicon.ico", "favicon.ico")
        val routes = staticRoutes.buildRoutes()
        assertThat(routes.size, equalTo(1))
        startServer(routes)

        RestAssured.given()
            .`when`()
            .get("$baseUrl/favicon.ico")
            .then()
            .statusCode(200)
            .body(equalTo(ico))
    }

    @Test
    fun subdirs() {
        val js = "const aVar = 1"
        val jsDir = Paths.get(filePath, "js")
        Files.createDirectory(jsDir)
        Files.write(jsDir.resolve("somejs.js"), js.toByteArray(StandardCharsets.UTF_8))

        staticRoutes.subDirs("js")
        val routes = staticRoutes.buildRoutes()
        assertThat(routes.size, equalTo(1))
        startServer(routes)

        RestAssured.given()
            .`when`()
            .get("$baseUrl/js/somejs.js")
            .then()
            .statusCode(200)
            .body(equalTo(js))
    }

    private fun startServer(routes: Iterable<Route>) {
        val latch = CountDownLatch(1)
        val router = Router.router(vertx)
        vertx.createHttpServer()
            .requestHandler(RouteRegister(router, "", true).add(routes).router)
            .listen(port) {
                latch.countDown()
                if (it.failed()) throw RuntimeException("Failed to start server")
            }

        latch.await()
    }

}
