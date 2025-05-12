/*
 * Copyright 2025 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.google.common.net.HttpHeaders
import com.google.common.net.MediaType
import com.zepben.annotations.EverythingIsNonnullByDefault
import com.zepben.vertxutils.json.filter.FilterSpecification
import com.zepben.vertxutils.json.filter.JsonObjectFilter
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Class that contains a bunch of helper functions for handling HTTP responses.
 */
@EverythingIsNonnullByDefault
object Respond {
    @JvmStatic
    @JvmOverloads
    fun with(
        context: RoutingContext,
        status: HttpResponseStatus,
        addHeaders: Map<String, String> = emptyMap()
    ) {
        context
            .response()
            .setStatusCode(status.code())
            .apply { if (addHeaders.isNotEmpty()) headers().addAll(addHeaders) }
            .end()
    }

    @JvmStatic
    fun with(
        context: RoutingContext,
        status: HttpResponseStatus,
        withEmptyContentLengthHeader: Boolean = false
    ) = with(
        context,
        status,
        if (withEmptyContentLengthHeader) mapOf(HttpHeaders.CONTENT_LENGTH to "0") else emptyMap()
    )

    @JvmStatic
    fun with(context: RoutingContext, response: Response) {
        context
            .response()
            .setStatusCode(response.status().code())
            .setStatusMessage(response.status().reasonPhrase())
            .apply { if (response.hasHeaders()) headers().addAll(response.headers()) }
            .end(response.body())
    }

    @JvmStatic
    @JvmOverloads
    fun withJson(
        context: RoutingContext,
        status: HttpResponseStatus,
        json: String,
        addHeaders: Map<String, String> = emptyMap()
    ) {
        context.response()
            .setStatusCode(status.code())
            .setStatusMessage(status.reasonPhrase())
            .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
            .apply { if (addHeaders.isNotEmpty()) headers().addAll(addHeaders) }
            .end(json)
    }

    @JvmStatic
    @JvmOverloads
    fun withJson(
        context: RoutingContext,
        status: HttpResponseStatus,
        json: JsonObject,
        filterSpecification: FilterSpecification,
        addHeaders: Map<String, String> = emptyMap()
    ) {
        context.response()
            .setStatusCode(status.code())
            .setStatusMessage(status.reasonPhrase())
            .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
            .apply { if (addHeaders.isNotEmpty()) headers().addAll(addHeaders) }
            .end(JsonObjectFilter.applyFilter(json, filterSpecification).encode())
    }

    // This function breaks the pattern and doesn't actually send the response, it just returns the unsent response.
    //  This is because EWB Network Routes needs it to behave this way and does further manipulation to it. Leave as is.
    @JvmStatic
    @JvmOverloads
    fun withJsonChunked(
        context: RoutingContext,
        status: HttpResponseStatus,
        addHeaders: Map<String, String> = emptyMap()
    ): HttpServerResponse {
        return context.response()
            .setStatusCode(status.code())
            .setChunked(true)
            .setStatusMessage(status.reasonPhrase())
            .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
            .apply { if (addHeaders.isNotEmpty()) headers().addAll(addHeaders) }
    }
}
