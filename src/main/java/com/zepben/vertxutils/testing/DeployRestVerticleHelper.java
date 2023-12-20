/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.testing;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class DeployRestVerticleHelper implements AutoCloseable {

    private final RequestSpecification requestSpec;
    private final Vertx vertx;

    public DeployRestVerticleHelper(Class<?> verticleClass, JsonObject config) {
        try {
            int port = getRandomPortNumber();
            config.put("http.port", port);

            // Start the server
            Promise<Void> promise = Promise.promise();
            Future<Void> future = promise.future();
            vertx = Vertx.vertx();
            DeploymentOptions options = new DeploymentOptions().setConfig(config);
            vertx.deployVerticle(verticleClass.getName(),
                options,
                ar -> {
                    if (ar.succeeded())
                        promise.complete();
                    else
                        promise.fail(ar.cause());
                });

            await().atMost(5, TimeUnit.SECONDS).until(future::isComplete);

            if (!future.succeeded())
                throw new AssertionError(future.cause().getMessage());

            requestSpec = new RequestSpecBuilder().setBaseUri("http://localhost").setPort(port).build();
        } catch (IOException ex) {
            throw new AssertionError("Failed to start server", ex);
        }
    }

    @Override
    public void close() {
        AtomicBoolean done = new AtomicBoolean(false);
        vertx.close(v -> done.set(true));
        await().until(done::get);
    }

    @SuppressWarnings("UnusedReturnValue")
    public RequestSpecification requestSpec() {
        return requestSpec;
    }

    public int getRandomPortNumber() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

}
