/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
@EverythingIsNonnullByDefault
public class RouteRegister {
    private final Router router;
    private final String rootMount;
    private final boolean defaultOrderedBlockingRoutes;
    private BiConsumer<String, Route> onAdd = (p, r) -> {
    };

    public RouteRegister(Router router, boolean defaultOrderedBlockingRoutes) {
        this(router, "", defaultOrderedBlockingRoutes);
    }

    public RouteRegister(Router router, String rootMount, boolean defaultOrderedBlockingRoutes) {
        this.router = router;
        this.rootMount = rootMount;
        this.defaultOrderedBlockingRoutes = defaultOrderedBlockingRoutes;
    }

    public Router router() {
        return router;
    }

    public RouteRegister onAdd(BiConsumer<String, Route> onAdd) {
        this.onAdd = onAdd;
        return this;
    }

    public RouteRegister add(Route route, String mountPath) {
        String path;
        io.vertx.ext.web.Route vertxRoute;
        if (route.path() == null) {
            path = "";
            vertxRoute = router.route();
        } else {
            path = buildPath(rootMount, mountPath, route.path());
            if (route.hasRegexPath())
                vertxRoute = router.routeWithRegex(path);
            else
                vertxRoute = router.route(path);
        }

        for (HttpMethod method : route.methods())
            vertxRoute.method(method);

        for (RouteHandler handler : route.handlers()) {
            if (handler.isBlocking()) {
                vertxRoute.blockingHandler(handler.handler(), handler.blockingOrdered().orElse(defaultOrderedBlockingRoutes));
            } else {
                vertxRoute.handler(handler.handler());
            }
        }

        for (Handler<RoutingContext> handler : route.failureHandlers())
            vertxRoute.failureHandler(handler);

        onAdd.accept(path, route);
        return this;
    }

    public RouteRegister add(Route route) {
        return add(route, "");
    }

    public RouteRegister add(RouteGroup group) {
        group.routes().forEach(route -> add(route, group.mountPath()));
        return this;
    }

    public RouteRegister add(Iterable<Route> routes) {
        routes.forEach(this::add);
        return this;
    }

    /**
     * Register a collection of route groups with this RouteRegister. NOTE: This function can't be named `add` like the others due
     * to type erasure making it have the same signature at the iterable for routes.
     *
     * @param routeGroups The collection of routes to register.
     * @return This RouteRegister for fluent use.
     */
    public RouteRegister addGroups(Iterable<RouteGroup> routeGroups) {
        routeGroups.forEach(this::add);
        return this;
    }

    private String buildPath(String rootMount, String mountPath, @Nullable String routePath) {
        String path = rootMount;
        if (!mountPath.isEmpty())
            path += "/" + mountPath;

        if (routePath == null) {
            // If the route path was null, it means match all paths, but now we are mounting it we need it to match
            // all paths below the mount point.
            if (!path.isEmpty())
                path += "/*";
        } else if (!routePath.isEmpty()) {
            if (!path.isEmpty() && !routePath.equals("$"))
                path += "/";

            path += routePath;
        }

        return path.replace("///", "/").replace("//", "/");
    }
}
