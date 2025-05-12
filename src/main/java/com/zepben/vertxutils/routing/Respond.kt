/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.json.filter.FilterSpecification;
import com.zepben.vertxutils.json.filter.JsonObjectFilter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Class that contains a bunch of helper functions for handling HTTP responses.
 */
@SuppressWarnings({"WeakerAccess", "UnstableApiUsage"})
@EverythingIsNonnullByDefault
public class Respond {

    public static void with(RoutingContext context, HttpResponseStatus status) {
        context.response()
            .setStatusCode(status.code())
            .end();
    }

    public static void with(RoutingContext context, Response response) {
        HttpServerResponse httpResponse = context.response();
        httpResponse.setStatusCode(response.status().code())
            .setStatusMessage(response.status().reasonPhrase());

        if (response.hasHeaders())
            httpResponse.headers().addAll(response.headers());

        httpResponse.end(response.body());
    }

    public static void withJson(RoutingContext context, HttpResponseStatus status, String json) {
        HttpServerResponse response = context.response()
            .setStatusCode(status.code())
            .setStatusMessage(status.reasonPhrase());

        response.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
            .end(json);
    }

    public static void withJson(RoutingContext context, HttpResponseStatus status, JsonObject json, FilterSpecification filterSpecification) {
        HttpServerResponse response = context.response()
            .setStatusCode(status.code())
            .setStatusMessage(status.reasonPhrase());

        response.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
            .end(JsonObjectFilter.applyFilter(json, filterSpecification).encode());
    }

    public static HttpServerResponse withJsonChunked(RoutingContext context, HttpResponseStatus status) {
        HttpServerResponse response = context.response()
            .setStatusCode(status.code())
            .setChunked(true)
            .setStatusMessage(status.reasonPhrase());

        response.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        return response;
    }

}
