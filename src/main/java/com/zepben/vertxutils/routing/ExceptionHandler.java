/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.function.BiConsumer;


public class ExceptionHandler<T extends Throwable> implements Handler<RoutingContext> {

    private final Class<T> tClass;
    private final BiConsumer<T, RoutingContext> handler;

    ExceptionHandler(Class<T> tClass, BiConsumer<T, RoutingContext> handler) {
        this.tClass = tClass;
        this.handler = handler;
    }

    @Override
    public void handle(RoutingContext context) {
        if (!tClass.isInstance(context.failure())) {
            context.next();
            return;
        }

        handle(tClass.cast(context.failure()), context);
    }

    private void handle(T throwable, RoutingContext handler) {
        this.handler.accept(tClass.cast(throwable), handler);
    }

}
