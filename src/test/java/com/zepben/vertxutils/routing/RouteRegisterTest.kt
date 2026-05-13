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
import com.zepben.vertxutils.routing.RouteGroup.Companion.create
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.*

class RouteRegisterTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val vertxRoute = mock<Route>()
    private val router = mock<Router>().also {
        doReturn(vertxRoute).`when`(it).route(anyString())
        doReturn(vertxRoute).`when`(it).routeWithRegex(anyString())
    }

    private val handler = Handler<RoutingContext?> {}
    private val blockingHandler = Handler<RoutingContext?> {}
    private val failureHandler = Handler<RoutingContext?> {}

    private var register = RouteRegister(router, "", true)

    @Test
    fun addRoutes() {
        val route = builder()
            .path("/some/path")
            .methods(HttpMethod.GET, HttpMethod.PUT)
            .addHandler(handler)
            .addBlockingHandler(blockingHandler)
            .addFailureHandler(failureHandler)
            .build()

        register.add(route, "")

        verify(router).route("/some/path")
        verify(vertxRoute).method(HttpMethod.GET)
        verify(vertxRoute).method(HttpMethod.PUT)

        val inOrder = inOrder(vertxRoute, vertxRoute)
        inOrder.verify(vertxRoute).handler(handler)
        inOrder.verify(vertxRoute).blockingHandler(blockingHandler, true)

        verify(vertxRoute).failureHandler(failureHandler)
    }

    @Test
    fun addRoutesDefaultOrderedBlockingTrue() {
        register = RouteRegister(router, "", true)

        val route = builder()
            .path("/some/path")
            .addBlockingHandler(blockingHandler)
            .build()

        register.add(route, "")

        verify(router).route("/some/path")
        verify(vertxRoute).blockingHandler(blockingHandler, true)
    }

    @Test
    fun addRoutesDefaultOrderedBlockingFalse() {
        register = RouteRegister(router, "", false)

        val route = builder()
            .path("/some/path")
            .addBlockingHandler(blockingHandler)
            .build()

        register.add(route, "")

        verify(router).route("/some/path")
        verify(vertxRoute).blockingHandler(blockingHandler, false)
    }

    @Test
    fun addRoutesOrderedBlockingTrue() {
        register = RouteRegister(router, "", false)

        val route = builder()
            .path("/some/path")
            .addBlockingHandler(blockingHandler, true)
            .build()

        register.add(route, "")

        verify(router).route("/some/path")
        verify(vertxRoute).blockingHandler(blockingHandler, true)
    }

    @Test
    fun addRoutesOrderedBlockingFalse() {
        register = RouteRegister(router, "", true)

        val route = builder()
            .path("/some/path")
            .addBlockingHandler(blockingHandler, false)
            .build()

        register.add(route, "")

        verify(router).route("/some/path")
        verify(vertxRoute).blockingHandler(blockingHandler, false)
    }

    @Test
    fun usesMountPaths() {
        register = RouteRegister(router, "/rootMount/", true)
        val route = builder()
            .path("/some/path")
            .build()

        register.add(route, "/mount/")

        verify(router).route("/rootMount/mount/some/path")
    }

    @Test
    fun addRouteGroup() {
        val route1 = builder().path("/route/1").build()
        val route2 = builder().path("/route/2").build()
        val group = create("/group", listOf(route1, route2))

        register.add(group)

        verify(router).route("/group/route/1")
        verify(router).route("/group/route/2")
    }

    @Test
    fun addRouteGroups() {
        val route1 = builder().path("/route/1").build()
        val route2 = builder().path("/route/2").build()
        val group1 = create("/group1", listOf(route1))
        val group2 = create("/group2", listOf(route2))

        register.addGroups(listOf(group1, group2))

        verify(router).route("/group1/route/1")
        verify(router).route("/group2/route/2")
    }

    @Test
    fun addsRegex() {
        val route = builder()
            .path("/some/regex/path")
            .hasRegexPath(true)
            .methods(HttpMethod.GET)
            .build()

        register.add(route, "")

        verify(router).routeWithRegex("/some/regex/path")
    }

    @Test
    fun usesMountPathRegex() {
        val route = builder()
            .path("/some/regex/path")
            .hasRegexPath(true)
            .build()

        register.add(route, "/mount")

        verify(router).routeWithRegex("/mount/some/regex/path")
    }

    @Test
    fun mountWithDollarRegexDoesNotAddSlash() {
        val route = builder()
            .path("$")
            .hasRegexPath(true)
            .build()

        register.add(route, "/mount")

        verify(router).routeWithRegex("/mount$")
    }

    @Test
    fun onAddCallback() {
        val path = "/some/regex/path"
        val route = builder()
            .path(path).build()

        val callback = mock<(String, com.zepben.vertxutils.routing.Route) -> Unit>()
        register.onAdd = callback
        register.add(route, "")
        verify(callback).invoke(path, route)

        val mount = "/my/mount"
        register.onAdd = callback
        register.add(route, mount)
        verify(callback).invoke(mount + path, route)

        val rootMount = "/our/root"
        val registerWithPath = RouteRegister(router, rootMount, true)

        registerWithPath.onAdd = callback
        registerWithPath.add(route, "")
        verify(callback).invoke(rootMount + path, route)

        registerWithPath.onAdd = callback
        registerWithPath.add(route, mount)
        verify(callback).invoke(rootMount + mount + path, route)
    }

}
