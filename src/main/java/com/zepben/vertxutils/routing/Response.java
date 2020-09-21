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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold things for a response to an HTTP request.
 */
@SuppressWarnings({"WeakerAccess", "UnstableApiUsage"})
@EverythingIsNonnullByDefault
public class Response {
    private HttpResponseStatus status;
    private Buffer body;
    @Nullable private Map<String, String> headers = null;

    public Response(HttpResponseStatus httpStatus) {
        this(httpStatus, Buffer.buffer());
    }

    public Response(HttpResponseStatus status, Buffer body) {
        this.status = status;
        this.body = body;
    }

    public static Response ofJson(HttpResponseStatus status, String json) {
        Response response = new Response(status, Buffer.buffer(json));
        response.headers().put(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        return response;
    }

    public static Response ofText(HttpResponseStatus status, String body) {
        Response response = new Response(status, Buffer.buffer(body));
        response.headers().put(HttpHeaders.CONTENT_TYPE, MediaType.PLAIN_TEXT_UTF_8.toString());
        return response;
    }

    public HttpResponseStatus status() {
        return status;
    }

    public Buffer body() {
        return body;
    }

    public Response setStatus(HttpResponseStatus status) {
        this.status = status;
        return this;
    }

    public Response setBody(Buffer body) {
        this.body = body;
        return this;
    }

    public boolean hasHeaders() {
        return headers != null && !headers.isEmpty();
    }

    public Map<String, String> headers() {
        if (headers == null)
            headers = new HashMap<>();

        return headers;
    }

}
