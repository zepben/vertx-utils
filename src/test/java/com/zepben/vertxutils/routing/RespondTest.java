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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class RespondTest {

    @RegisterExtension
    SystemLogExtension systemErr = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess();

    private final RoutingContext context = mock(RoutingContext.class);
    private final HttpServerResponse response = mock(HttpServerResponse.class, RETURNS_SELF);

    @BeforeEach
    public void setUp() {
        doReturn(response).when(context).response();
    }

    @Test
    public void withStatus() {
        Respond.with(context, HttpResponseStatus.OK);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).end();
    }

    @Test
    public void withStatusPlusHeaders() {
        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Map<String, String> addHeaders = Map.of("X-Test-Header", "value", "X-Test-Header-2", "value2");
        Respond.with(context, HttpResponseStatus.OK, addHeaders);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).end();
    }

    @Test
    public void withStatusPlusEmptyContentLengthHeader() {
        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Map<String, String> addHeaders = Map.of(HttpHeaders.CONTENT_LENGTH, "0");
        Respond.with(context, HttpResponseStatus.OK, true);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).end();
    }

    @Test
    public void withResponse() {
        Response response = Response.ofText(HttpResponseStatus.OK, "test");
        MultiMap headers = mock(MultiMap.class);
        doReturn(headers).when(this.response).headers();
        Respond.with(context, response);
        verify(this.response).setStatusCode(HttpResponseStatus.OK.code());
        verify(headers).addAll(response.headers());
        verify(this.response).end(response.body());
    }

    @Test
    public void withJson() {
        Respond.withJson(context, HttpResponseStatus.OK, "json");
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(response).end("json");
    }

    @Test
    public void withJsonPlusHeaders() {
        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Map<String, String> addHeaders = Map.of("X-Test-Header", "value", "X-Test-Header-2", "value2");

        Respond.withJson(context, HttpResponseStatus.OK, "json", addHeaders);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).end("json");
    }

    @Test
    public void withJsonFilter() throws Exception {
        FilterSpecification filterSpecification = new FilterSpecification("a.b");

        JsonObject jsonObject = new JsonObject()
            .put("a", new JsonObject()
                .put("b", 1)
                .put("c", 2))
            .put("d", 3);

        Respond.withJson(context, HttpResponseStatus.OK, jsonObject, filterSpecification);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(response).end("{\"a\":{\"b\":1}}");
    }

    @Test
    public void withJsonFilterPlusHeaders() throws Exception {
        FilterSpecification filterSpecification = new FilterSpecification("a.b");

        JsonObject jsonObject = new JsonObject()
                .put("a", new JsonObject()
                        .put("b", 1)
                        .put("c", 2))
                .put("d", 3);

        MultiMap existingHeaders = mock(MultiMap.class);
        doReturn(existingHeaders).when(this.response).headers();
        Map<String, String> addHeaders = Map.of("X-Test-Header", "value", "X-Test-Header-2", "value2");

        Respond.withJson(context, HttpResponseStatus.OK, jsonObject, filterSpecification, addHeaders);
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).end("{\"a\":{\"b\":1}}");
    }

    @Test
    public void withJsonChunked() {
        HttpServerResponse returnedResponse = Respond.withJsonChunked(context, HttpResponseStatus.OK);
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

        HttpServerResponse returnedResponse = Respond.withJsonChunked(context, HttpResponseStatus.OK, addHeaders);
        assertThat(returnedResponse, is(response));
        verify(response).setStatusCode(HttpResponseStatus.OK.code());
        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(existingHeaders).addAll(addHeaders);
        verify(response).setChunked(true);
    }

}
