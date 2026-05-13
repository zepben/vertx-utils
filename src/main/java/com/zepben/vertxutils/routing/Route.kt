/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.vertxutils.routing.handlers.DecodeBodyHandler
import com.zepben.vertxutils.routing.handlers.PathParamsHandler
import com.zepben.vertxutils.routing.handlers.QueryParamsHandler
import com.zepben.vertxutils.routing.handlers.params.BodyRule
import com.zepben.vertxutils.routing.handlers.params.PathParamRule
import com.zepben.vertxutils.routing.handlers.params.QueryParamRule
import com.zepben.vertxutils.routing.handlers.params.RequestValueConverter
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RequestBody
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

/**
 * @property path The path of the route.
 * @property hasRegexPath Set to true if the path uses regular expressions. Defaults to false.
 * @property methods The HTTP method for the route.
 * @property handlers A list of handlers for this route. Remember to always call [RoutingContext.next] to chain to your next handler if you have more than one.
 * @property failureHandlers The failure handlers for the route.
 * @property isPublic Indicates if the route should be documented as a public route. True if the route is a publicly documented route.
 */
class Route private constructor(
    val path: String?,
    val hasRegexPath: Boolean,
    val methods: Set<HttpMethod>,
    val handlers: List<RouteHandler>,
    val failureHandlers: List<Handler<RoutingContext>>,
    val isPublic: Boolean,
) {

    class Builder internal constructor() {

        private var path: String? = null
        private var hasRegexPath = false
        private val methods = mutableSetOf<HttpMethod>()
        private var pathParamsHandler: PathParamsHandler? = null
        private var queryParamsHandler: QueryParamsHandler? = null
        private var bodyHandler: BodyHandler? = null
        private var decodeBodyHandler: DecodeBodyHandler? = null
        private val handlers = mutableListOf<RouteHandler>()
        private val failureHandlers = mutableListOf<Handler<RoutingContext>>()
        private var isPublic = true

        fun path(path: String): Builder = also { builder ->
            require(!path.isEmpty()) { "path must not be empty" }
            require(path.indexOf('%') < 0) { "formatted path must not contain a '%'" }

            builder.path = path
        }

        fun path(pathFormat: String, vararg rules: PathParamRule<*>): Builder = apply {
            var count = 0
            var index = pathFormat.indexOf('%')
            while (index >= 0) {
                ++count
                require((index != 0) && (index < (pathFormat.length - 1)) && (pathFormat[index - 1] == ':') && (pathFormat[index + 1] == 's')) {
                    "invalid use of % in path format string"
                }
                index = pathFormat.indexOf('%', index + 1)
            }

            require(count >= rules.size) { "too many path params" }
            require(count <= rules.size) { "missing path params" }

            path(String.format(pathFormat, *rules.map { it.name }.toTypedArray()))
            pathParamsHandler = PathParamsHandler(*rules)
        }

        fun hasRegexPath(hasRegexPath: Boolean): Builder = also { builder ->
            builder.hasRegexPath = hasRegexPath
        }

        fun method(method: HttpMethod): Builder = apply {
            methods.add(method)
        }

        fun methods(vararg methods: HttpMethod): Builder = apply {
            for (method in methods)
                method(method)
        }

        fun queryParams(vararg rules: QueryParamRule<*>): Builder = apply {
            queryParamsHandler = QueryParamsHandler(*rules)
        }

        fun bodySizeLimit(size: Long): Builder = apply {
            if (bodyHandler == null)
                bodyHandler(BodyHandler.create())

            bodyHandler!!.setBodyLimit(size)
        }

        fun uploadsDirectory(path: String): Builder = apply {
            if (bodyHandler == null)
                bodyHandler(BodyHandler.create())

            bodyHandler!!.setUploadsDirectory(path)
        }

        fun decodeBody(bodyConverter: RequestValueConverter<RequestBody, *>, bodyRequired: Boolean = true): Builder = apply {
            if (bodyHandler == null)
                bodyHandler(BodyHandler.create())

            decodeBodyHandler(DecodeBodyHandler(BodyRule(bodyConverter, bodyRequired)))
        }

        fun bodyHandler(handler: BodyHandler): Builder = apply {
            bodyHandler = handler
        }

        fun decodeBodyHandler(handler: DecodeBodyHandler): Builder = apply {
            decodeBodyHandler = handler
        }

        fun addHandler(handler: RouteHandler): Builder = apply {
            handlers.add(handler)
        }

        fun addHandler(handler: Handler<RoutingContext>): Builder =
            addHandler(RouteHandler(handler, false))

        /**
         * Registers a blocking handler.
         * This makes the handler equivalent to being registered with [io.vertx.ext.web.Route.blockingHandler].
         * on the [RouteRegister] however the boolean ordered flag is set by the argument given to the route register.
         * 
         * @param blockingHandler The handler that contains blocking code.
         * @return This builder.
         */
        fun addBlockingHandler(blockingHandler: Handler<RoutingContext>): Builder =
            addHandler(RouteHandler(blockingHandler, true, null))

        /**
         * Registers a blocking handler.
         * This makes the handler equivalent to being registered with [io.vertx.ext.web.Route.blockingHandler]
         * on the [RouteRegister].
         * 
         * @param blockingHandler The handler that contains blocking code.
         * @return This builder.
         */
        fun addBlockingHandler(blockingHandler: Handler<RoutingContext>, ordered: Boolean): Builder =
            addHandler(RouteHandler(blockingHandler, true, ordered))

        fun addFailureHandler(failureHandler: Handler<RoutingContext>): Builder = apply {
            failureHandlers.add(failureHandler)
        }

        fun <T : Throwable> addFailureHandler(throwableClass: Class<T>, handler: (T, RoutingContext?) -> Unit): Builder = apply {
            failureHandlers.add(ExceptionHandler(throwableClass, handler))
        }

        fun isPublic(isPublic: Boolean): Builder = also { builder ->
            builder.isPublic = isPublic
        }

        fun build(): Route {
            check((path == null) || hasRegexPath || (path!![0] == '/')) { "path must start with a /" }

            val allHandlers = listOfNotNull(
                bodyHandler,
                decodeBodyHandler.takeIf { bodyHandler != null },
                pathParamsHandler,
                queryParamsHandler,
            ).map { RouteHandler(it, false) } +
                handlers

            return Route(
                path,
                hasRegexPath,
                methods,
                allHandlers,
                failureHandlers,
                isPublic,
            )
        }
    }

    companion object {

        fun builder(): Builder = Builder()

    }

}
