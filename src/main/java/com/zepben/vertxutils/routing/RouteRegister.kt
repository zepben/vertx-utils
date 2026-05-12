/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import io.vertx.ext.web.Router

class RouteRegister(
    val router: Router,
    val rootMount: String = "",
    val defaultOrderedBlockingRoutes: Boolean,
) {

    var onAdd: (path: String, route: Route) -> Unit = { _, _ -> }

    fun add(route: Route, mountPath: String = ""): RouteRegister = apply {
        val path = route.path?.let { buildPath(rootMount, mountPath, route.path) }

        val vertxRoute = when {
            path == null -> router.route()
            route.hasRegexPath -> router.routeWithRegex(path)
            else -> router.route(path)
        }

        route.methods.forEach { vertxRoute.method(it) }

        route.handlers.forEach {
            when {
                it.isBlocking -> vertxRoute.blockingHandler(it.handler, it.blockingOrdered ?: defaultOrderedBlockingRoutes)
                else -> vertxRoute.handler(it.handler)
            }
        }

        route.failureHandlers.forEach { vertxRoute.failureHandler(it) }

        onAdd(path ?: "", route)
    }

    fun add(group: RouteGroup): RouteRegister = apply {
        group.routes.forEach { route -> add(route, group.mountPath) }
    }

    fun add(routes: Iterable<Route>): RouteRegister = apply {
        routes.forEach { route -> add(route) }
    }

    /**
     * Register a collection of route groups with this RouteRegister. NOTE: This function can't be named `add` like the others due
     * to type erasure making it have the same signature at the iterable for routes.
     * 
     * @param routeGroups The collection of routes to register.
     * @return This RouteRegister for fluent use.
     */
    fun addGroups(routeGroups: Iterable<RouteGroup>): RouteRegister = apply {
        routeGroups.forEach { group -> add(group) }
    }

    private fun buildPath(rootMount: String, mountPath: String, routePath: String?): String {
        var path = rootMount
        if (!mountPath.isEmpty()) path += "/$mountPath"

        if (routePath == null) {
            // If the route path was null, it means match all paths, but now we are mounting it we need it to match
            // all paths below the mount point.
            if (!path.isEmpty())
                path += "/*"
        } else if (routePath.isNotEmpty()) {
            if (path.isNotEmpty() && routePath != "$")
                path += "/"

            path += routePath
        }

        return path.replace("///", "/").replace("//", "/")
    }

}
