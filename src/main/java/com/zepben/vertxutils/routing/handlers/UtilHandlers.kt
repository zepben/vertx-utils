/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers

import com.zepben.vertxutils.routing.ErrorFormatter
import com.zepben.vertxutils.routing.Respond
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.core.VertxException
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger

object UtilHandlers {

    /**
     * Returns the default failure handler that if the [RoutingContext.failure] is not null responds with a
     * 500 message with our own "standardised" errors JSON response (See [ErrorFormatter.asJson])
     * containing the message from the failure. If a logger is specified, it will also log the stacktrace on the server side
     */
    val CATCH_ALL_API_FAILURE_HANDLER_WITH_EXCEPTION_LOGGING: (Logger?) -> Handler<RoutingContext> = { logger ->
        Handler { context: RoutingContext ->
            val failure = context.failure()
            if (failure != null && !context.response().ended()) {
                logger?.error("Error stack trace:", failure)

                Respond.withJson(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, ErrorFormatter.asJson(failure.toString()))
                return@Handler
            } else if (failure is VertxException && failure.message == "Connection was closed") {
                // Don't call context.next() in this case because it logs it. We don't care.
                return@Handler
            }
            context.next()
        }
    }

    val CATCH_ALL_API_FAILURE_HANDLER: Handler<RoutingContext> = { context ->
        CATCH_ALL_API_FAILURE_HANDLER_WITH_EXCEPTION_LOGGING(null).handle(context)
    }

    /**
     * Route handler to redirect routes with no trailing slash to one with a trailing slash.
     */
    val REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER: Handler<RoutingContext> = Handler { context ->
        var location = "${context.request()?.path()}/${if (context.request().query() != null) "?" + context.request().query() else ""}"

        context.response().putHeader("Location", location)
        Respond.with(context, HttpResponseStatus.MOVED_PERMANENTLY)
    }

}
