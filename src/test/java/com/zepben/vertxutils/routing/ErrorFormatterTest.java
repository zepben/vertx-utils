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
import com.zepben.vertxutils.routing.handlers.UtilHandlers;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@SuppressWarnings("UnstableApiUsage")
public class ErrorFormatterTest {

    @SuppressWarnings("ThrowableNotThrown")
    @Test
    public void defaultFailureHandler() {
        RoutingContext context = mock(RoutingContext.class);
        HttpServerResponse response = mock(HttpServerResponse.class, RETURNS_SELF);
        doReturn(response).when(context).response();

        Throwable failure = new RuntimeException("test");
        doReturn(failure).when(context).failure();
        UtilHandlers.CATCH_ALL_API_FAILURE_HANDLER.handle(context);

        verify(response).putHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
        verify(response).end(ErrorFormatter.asJson(failure.toString()));
    }

    @Test
    public void redirectNoTrailingToTrailing() {
        RoutingContext context = mock(RoutingContext.class);
        HttpServerRequest request = mock(HttpServerRequest.class);
        HttpServerResponse response = mock(HttpServerResponse.class, RETURNS_SELF);
        doReturn(request).when(context).request();
        doReturn(response).when(context).response();
        doReturn("/some/path/without/slash").when(request).path();
        doReturn("test=true").when(request).query();

        UtilHandlers.REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER.handle(context);

        verify(response).putHeader("Location", "/some/path/without/slash/?test=true");
        verify(response).setStatusCode(301);
        verify(response).end();
    }

    @Test
    public void errorToJson() {
        String err = "err";
        String actual = ErrorFormatter.asJson(err);
        String expected = new JsonObject().put("errors", Collections.singletonList(err)).encode();
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void errorsToJson() {
        List<String> errs = Arrays.asList("err1", "err2");
        String actual = ErrorFormatter.asJson(errs);
        String expected = new JsonObject().put("errors", errs).encode();
        assertThat(actual, equalTo(expected));
    }

}
