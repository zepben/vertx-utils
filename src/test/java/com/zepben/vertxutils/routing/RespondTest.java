/*
 * Copyright 2025 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.zepben.testutils.junit.SystemLogExtension;
import com.zepben.vertxutils.json.filter.FilterSpecification;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class RespondTest {

    @RegisterExtension
    static SystemLogExtension systemErr = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess();

    private final RoutingContext context = mock(RoutingContext.class);
    private final HttpServerResponse response = mock(HttpServerResponse.class, RETURNS_SELF);

    @BeforeEach
    public void setUp() {
        doReturn(response).when(context).response();
    }

    @Test
    public void withStatus() {
        Respond.INSTANCE.with(context, HttpResponseStatus.OK, emptyMap(), false);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).end();
    }

    @Test
    public void withStatusPlusHeaders() {
        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Map<String, String> addHeaders = Map.of("X-Test-Header", "value", "X-Test-Header-2", "value2");
        Respond.INSTANCE.with(context, HttpResponseStatus.OK, addHeaders, false);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).end();
    }

    @Test
    public void withStatusPlusEmptyContentLengthHeader() {
        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Respond.INSTANCE.with(context, HttpResponseStatus.OK, emptyMap(), true);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(existingHeaders).set(HttpHeaders.CONTENT_LENGTH, "0");
        verify(response).end();
    }

    @Test
    public void withResponse() {
        Response response = Response.Companion.ofText(HttpResponseStatus.OK, "test");
        MultiMap headers = mock(MultiMap.class);
        doReturn(headers).when(this.response).headers();
        Respond.INSTANCE.with(context, response);
        verify(this.response).setStatusCode(HttpResponseStatus.OK.code());
        verify(headers).addAll(response.getHeaders());
        verify(this.response).end(response.getBody());
    }

    @Test
    public void withJson() {
        Respond.INSTANCE.withJson(context, HttpResponseStatus.OK, "json", emptyMap());
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(response).end("json");
    }

    @Test
    public void withJsonPlusHeaders() {
        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Map<String, String> addHeaders = Map.of("X-Test-Header", "value", "X-Test-Header-2", "value2");

        Respond.INSTANCE.withJson(context, HttpResponseStatus.OK, "json", addHeaders);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).end("json");
    }

    @Test
    public void withJsonFilter() {
        FilterSpecification filterSpecification = new FilterSpecification("a.b");

        JsonObject jsonObject = new JsonObject()
            .put("a", new JsonObject()
                .put("b", 1)
                .put("c", 2))
            .put("d", 3);

        Respond.INSTANCE.withJson(context, HttpResponseStatus.OK, jsonObject, filterSpecification, emptyMap());
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(response).end("{\"a\":{\"b\":1}}");
    }

    @Test
    public void withJsonFilterPlusHeaders() {
        FilterSpecification filterSpecification = new FilterSpecification("a.b");

        JsonObject jsonObject = new JsonObject()
            .put("a", new JsonObject()
                .put("b", 1)
                .put("c", 2))
            .put("d", 3);

        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Map<String, String> addHeaders = Map.of("X-Test-Header", "value", "X-Test-Header-2", "value2");

        Respond.INSTANCE.withJson(context, HttpResponseStatus.OK, jsonObject, filterSpecification, addHeaders);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).end("{\"a\":{\"b\":1}}");
    }

    @Test
    public void withJsonChunked() {
        HttpServerResponse returnedResponse = Respond.INSTANCE.withJsonChunked(context, HttpResponseStatus.OK, emptyMap());
        assertThat(returnedResponse, is(response));
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(response).setChunked(true);
    }

    @Test
    public void withJsonChunkedPlusHeaders() {
        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Map<String, String> addHeaders = Map.of("X-Test-Header", "value", "X-Test-Header-2", "value2");

        HttpServerResponse returnedResponse = Respond.INSTANCE.withJsonChunked(context, HttpResponseStatus.OK, addHeaders);
        assertThat(returnedResponse, is(response));
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).setChunked(true);
    }

}
