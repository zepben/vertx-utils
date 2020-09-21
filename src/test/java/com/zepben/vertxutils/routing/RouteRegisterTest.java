/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;

public class RouteRegisterTest {

    private final Router router = mock(Router.class);
    private final io.vertx.ext.web.Route vertxRoute = mock(io.vertx.ext.web.Route.class);

    private RouteRegister register = new RouteRegister(router, true);

    @BeforeEach
    public void setUp() {
        doReturn(vertxRoute).when(router).route(any());
        doReturn(vertxRoute).when(router).routeWithRegex(any());
    }

    @Test
    public void addRoutes() {
        Handler<RoutingContext> handler = c -> {
        };
        Handler<RoutingContext> blockingHandler = c -> {
        };
        Handler<RoutingContext> failureHandler = c -> {
        };

        Route route = Route.builder()
            .path("/some/path")
            .methods(HttpMethod.GET, HttpMethod.PUT)
            .addHandler(handler)
            .addBlockingHandler(blockingHandler)
            .addFailureHandler(failureHandler)
            .build();

        register.add(route);

        verify(router).route("/some/path");
        verify(vertxRoute).method(HttpMethod.GET);
        verify(vertxRoute).method(HttpMethod.PUT);

        InOrder inOrder = inOrder(vertxRoute, vertxRoute);
        inOrder.verify(vertxRoute).handler(handler);
        inOrder.verify(vertxRoute).blockingHandler(blockingHandler, true);

        verify(vertxRoute).failureHandler(failureHandler);
    }

    @Test
    public void addRoutesDefaultOrderedBlockingTrue() {
        register = new RouteRegister(router, true);

        Handler<RoutingContext> blockingHandler = c -> {
        };

        Route route = Route.builder()
            .path("/some/path")
            .addBlockingHandler(blockingHandler)
            .build();

        register.add(route);

        verify(router).route("/some/path");
        verify(vertxRoute).blockingHandler(blockingHandler, true);
    }

    @Test
    public void addRoutesDefaultOrderedBlockingFalse() {
        register = new RouteRegister(router, false);

        Handler<RoutingContext> blockingHandler = c -> {
        };

        Route route = Route.builder()
            .path("/some/path")
            .addBlockingHandler(blockingHandler)
            .build();

        register.add(route);

        verify(router).route("/some/path");
        verify(vertxRoute).blockingHandler(blockingHandler, false);
    }

    @Test
    public void addRoutesOrderedBlockingTrue() {
        register = new RouteRegister(router, false);

        Handler<RoutingContext> blockingHandler = c -> {
        };

        Route route = Route.builder()
            .path("/some/path")
            .addBlockingHandler(blockingHandler, true)
            .build();

        register.add(route);

        verify(router).route("/some/path");
        verify(vertxRoute).blockingHandler(blockingHandler, true);
    }

    @Test
    public void addRoutesOrderedBlockingFalse() {
        register = new RouteRegister(router, true);

        Handler<RoutingContext> blockingHandler = c -> {
        };

        Route route = Route.builder()
            .path("/some/path")
            .addBlockingHandler(blockingHandler, false)
            .build();

        register.add(route);

        verify(router).route("/some/path");
        verify(vertxRoute).blockingHandler(blockingHandler, false);
    }

    @Test
    public void usesMountPaths() {
        register = new RouteRegister(router, "/rootMount/", true);
        Route route = Route.builder()
            .path("/some/path")
            .build();

        register.add(route, "/mount/");

        verify(router).route("/rootMount/mount/some/path");
    }

    @Test
    public void addRouteGroup() {
        Route route1 = Route.builder().path("/route/1").build();
        Route route2 = Route.builder().path("/route/2").build();
        RouteGroup group = RouteGroup.create("/group", Arrays.asList(route1, route2));

        register.add(group);

        verify(router).route("/group/route/1");
        verify(router).route("/group/route/2");
    }

    @Test
    public void addsRegex() {
        Route route = Route.builder()
            .path("/some/regex/path")
            .hasRegexPath(true)
            .methods(HttpMethod.GET)
            .build();

        register.add(route);

        verify(router).routeWithRegex("/some/regex/path");
    }

    @Test
    public void usesMountPathRegex() {
        Route route = Route.builder()
            .path("/some/regex/path")
            .hasRegexPath(true)
            .build();

        register.add(route, "/mount");

        verify(router).routeWithRegex("/mount/some/regex/path");
    }

    @Test
    public void mountWithDollarRegexDoesNotAddSlash() {
        Route route = Route.builder()
            .path("$")
            .hasRegexPath(true)
            .build();

        register.add(route, "/mount");

        verify(router).routeWithRegex("/mount$");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onAddCallback() {
        String path = "/some/regex/path";
        Route route = Route.builder()
            .path(path).build();

        BiConsumer<String, Route> callback = mock(BiConsumer.class);
        register.onAdd(callback)
            .add(route);
        verify(callback).accept(path, route);

        String mount = "/my/mount";
        register.onAdd(callback)
            .add(route, mount);
        verify(callback).accept(mount + path, route);

        String rootMount = "/our/root";
        RouteRegister registerWithPath = new RouteRegister(router, rootMount, true);

        registerWithPath.onAdd(callback)
            .add(route);
        verify(callback).accept(rootMount + path, route);

        registerWithPath.onAdd(callback)
            .add(route, mount);
        verify(callback).accept(rootMount + mount + path, route);
    }
}
