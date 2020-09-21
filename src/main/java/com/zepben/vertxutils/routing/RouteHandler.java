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
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nullable;
import java.util.Optional;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class RouteHandler {

    private final Handler<RoutingContext> handler;
    private final boolean isBlocking;
    @Nullable private final Boolean blockingOrdered;

    public RouteHandler(Handler<RoutingContext> handler, boolean isBlocking) {
        this(handler, isBlocking, null);
    }

    public RouteHandler(Handler<RoutingContext> handler, boolean isBlocking, @Nullable Boolean blockingOrdered) {
        this.handler = handler;
        this.isBlocking = isBlocking;
        this.blockingOrdered = blockingOrdered;
    }

    public Handler<RoutingContext> handler() {
        return handler;
    }

    public boolean isBlocking() {
        return isBlocking;
    }

    public Optional<Boolean> blockingOrdered() {
        return Optional.ofNullable(blockingOrdered);
    }
}
