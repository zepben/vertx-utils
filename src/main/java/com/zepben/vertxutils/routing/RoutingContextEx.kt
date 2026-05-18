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
import com.zepben.vertxutils.routing.handlers.params.BadParamException
import com.zepben.vertxutils.routing.handlers.params.PathParams
import com.zepben.vertxutils.routing.handlers.params.QueryParams
import io.vertx.ext.web.RoutingContext

/**
 * These would ideally be extension methods for [RoutingContext] but stupid Java doesn't have them.
 */
object RoutingContextEx {

    val PATH_PARAMS_KEY: String = PathParamsHandler::class.java.getSimpleName()
    val QUERY_PARAMS_KEY: String = QueryParamsHandler::class.java.getSimpleName()
    val BODY_KEY: String = DecodeBodyHandler::class.java.getSimpleName()

    fun getPathParams(context: RoutingContext): PathParams {
        val params = context.get<PathParams>(PATH_PARAMS_KEY)
        checkNotNull(params) { "PathParamsHandler must be called before you can use RoutingContextEx.getPathParams" }

        return params
    }

    fun putPathParams(context: RoutingContext, params: PathParams) {
        context.put(PATH_PARAMS_KEY, params)
    }

    fun getQueryParams(context: RoutingContext): QueryParams {
        val params = context.get<QueryParams>(QUERY_PARAMS_KEY)
        checkNotNull(params) { "QueryParamsHandler must be called before you can use RoutingContextEx.getQueryParams" }

        return params
    }

    fun putQueryParams(context: RoutingContext, params: QueryParams) {
        context.put(QUERY_PARAMS_KEY, params)
    }

    fun <T> getDecodedBody(context: RoutingContext): T =
        context.get<T>(BODY_KEY) ?: throw BadParamException.missingBody()

    fun <T> getOptionalDecodedBody(context: RoutingContext): T? =
        context.get<T>(BODY_KEY)

    fun putRequestBody(context: RoutingContext, body: Any) {
        context.put(BODY_KEY, body)
    }

}
