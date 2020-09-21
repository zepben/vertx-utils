/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.routing.ErrorFormatter;
import com.zepben.vertxutils.routing.Respond;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.ext.web.RoutingContext;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.MOVED_PERMANENTLY;

@EverythingIsNonnullByDefault
public class UtilHandlers {

    /**
     * A default failure handler that if the {@link RoutingContext#failure()} is not null responds with a
     * 500 message with our own "standardised" errors JSON response (See {@link ErrorFormatter#asJson(String)})
     * containing the message from the failure.
     */
    public static final Handler<RoutingContext> CATCH_ALL_API_FAILURE_HANDLER = context -> {
        Throwable failure = context.failure();
        if (failure != null && !context.response().ended()) {
            Respond.withJson(context, INTERNAL_SERVER_ERROR, ErrorFormatter.asJson(failure.toString()));
            return;
        } else if (failure instanceof VertxException && failure.getMessage().equals("Connection was closed")) {
            // Don't call context.next() in this case because it logs it. We don't care.
            return;
        }

        context.next();
    };

    @Deprecated
    public static final Handler<RoutingContext> DEFAULT_FAILURE_HANDLER = CATCH_ALL_API_FAILURE_HANDLER;

    /**
     * Route handler to redirect routes with no trailing slash to one with a trailing slash.
     */
    public static final Handler<RoutingContext> REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER = context -> {
        String location = context.request().path() + "/";
        if (context.request().query() != null)
            location += "?" + context.request().query();

        context.response().putHeader("Location", location);
        Respond.with(context, MOVED_PERMANENTLY);
    };
}
