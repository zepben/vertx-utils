/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.testing

import com.zepben.vertxutils.routing.RoutingContextEx
import com.zepben.vertxutils.routing.handlers.params.PathParamRule
import com.zepben.vertxutils.routing.handlers.params.PathParams
import com.zepben.vertxutils.routing.handlers.params.QueryParamRule
import com.zepben.vertxutils.routing.handlers.params.QueryParams
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.mockito.Mockito.*

object MockRoutingContext {

    fun builder(): Builder = Builder()

    class Builder internal constructor() {

        private var pathParams: PathParams? = null
        private val pathParamsMap = mutableMapOf<String, Any>()
        private var queryParams: QueryParams? = null
        private val queryParamsMap = mutableMapOf<String, MutableList<Any>>()
        private val queryParamRules = mutableSetOf<QueryParamRule<*>>()
        private var decodedBody: Any? = null

        fun build(): RoutingContext {
            val context = mock(RoutingContext::class.java)
            val request = mock(HttpServerRequest::class.java, RETURNS_SELF)
            val response = mock(HttpServerResponse::class.java, RETURNS_SELF)

            doReturn(request).`when`(context).request()
            doReturn(response).`when`(context).response()

            doReturn(pathParams ?: run { PathParams(pathParamsMap) })
                .`when`(context).get<Any>(RoutingContextEx.PATH_PARAMS_KEY)

            doReturn(queryParams ?: run { QueryParams(queryParamRules, queryParamsMap) })
                .`when`(context).get<Any>(RoutingContextEx.QUERY_PARAMS_KEY)

            doReturn(decodedBody).`when`(context).get<Any>(RoutingContextEx.BODY_KEY)

            return context
        }

        fun pathParams(params: PathParams): Builder = apply {
            pathParams = params
        }

        fun pathParam(rule: PathParamRule<*>, value: Any): Builder = apply {
            pathParamsMap[rule.name] = value
        }

        fun queryParams(params: QueryParams): Builder = apply {
            queryParams = params
        }

        fun queryParam(rule: QueryParamRule<*>): Builder = apply {
            queryParams(rule)
        }

        fun queryParams(vararg rule: QueryParamRule<*>): Builder = apply {
            queryParamRules.addAll(listOf(*rule))
        }

        fun queryParam(rule: QueryParamRule<*>, vararg values: Any): Builder = apply {
            queryParam(rule)
            queryParamsMap.getOrPut(rule.name) { mutableListOf() }.addAll(values)
        }

        fun decodedBody(decodedBody: Any): Builder = also {
            it.decodedBody = decodedBody
        }

    }

}
