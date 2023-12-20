/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.testing;

import com.zepben.vertxutils.routing.Route;
import com.zepben.vertxutils.routing.RouteRegister;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("WeakerAccess")
public class TestHttpServer implements AutoCloseable {

    private final Vertx vertx;
    private final HttpServer server;
    private final Router router;
    private final RouteRegister routeRegister;

    public TestHttpServer() {
        this(true);
    }

    public TestHttpServer(boolean orderBlockingRules) {
        vertx = Vertx.vertx();
        server = vertx.createHttpServer();
        router = Router.router(vertx);
        routeRegister = new RouteRegister(router, orderBlockingRules);
    }

    public TestHttpServer addRoute(Route route) {
        routeRegister.add(route);
        return this;
    }

    public TestHttpServer addRoutes(Iterable<Route> routes) {
        routeRegister.add(routes);
        return this;
    }

    public int listen() {
        CountDownLatch latch = new CountDownLatch(1);
        server.requestHandler(router)
            .listen(getRandomPortNumber(), res -> {
                if (res.failed())
                    throw new RuntimeException(res.cause());

                latch.countDown();
            });

        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }

        return server.actualPort();
    }

    @Override
    public void close() {
        CountDownLatch latch = new CountDownLatch(2);
        server.close(none -> latch.countDown());
        vertx.close(none -> latch.countDown());

        try {
            latch.await();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private int getRandomPortNumber() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
