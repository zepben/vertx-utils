/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.vertxutils.routing.handlers.params.BadParamException;
import com.zepben.vertxutils.routing.handlers.params.PathParams;
import com.zepben.vertxutils.routing.handlers.params.QueryParams;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class RoutingContextExTest {

    private final RoutingContext context = mock(RoutingContext.class);

    @Test
    public void keysAreDifferent() {
        assertThat(RoutingContextEx.PATH_PARAMS_KEY, not(equalTo(RoutingContextEx.QUERY_PARAMS_KEY)));
        assertThat(RoutingContextEx.PATH_PARAMS_KEY, not(equalTo(RoutingContextEx.BODY_KEY)));
    }

    @Test
    public void getPathParams() {
        PathParams params = new PathParams(Collections.emptyMap());
        doReturn(params).when(context).get(RoutingContextEx.PATH_PARAMS_KEY);
        assertThat(RoutingContextEx.getPathParams(context), is(params));
    }

    @Test
    public void getPathParamsWhenNoParams() {
        expect(() -> RoutingContextEx.getPathParams(context)).toThrow(IllegalStateException.class);
    }

    @Test
    public void putPathParams() {
        PathParams params = new PathParams(Collections.emptyMap());
        RoutingContextEx.putPathParams(context, params);
        verify(context).put(RoutingContextEx.PATH_PARAMS_KEY, params);
    }

    @Test
    public void getQueryParams() {
        QueryParams params = mock(QueryParams.class);
        doReturn(params).when(context).get(RoutingContextEx.QUERY_PARAMS_KEY);
        assertThat(RoutingContextEx.getQueryParams(context), is(params));
    }

    @Test
    public void getQueryParamsWhenNoParams() {
        expect(() -> RoutingContextEx.getQueryParams(context)).toThrow(IllegalStateException.class);
    }

    @Test
    public void putQueryParams() {
        QueryParams params = mock(QueryParams.class);
        RoutingContextEx.putQueryParams(context, params);
        verify(context).put(RoutingContextEx.QUERY_PARAMS_KEY, params);
    }

    @Test
    public void getDecodedBody() {
        doReturn("expected").when(context).get(RoutingContextEx.BODY_KEY);
        assertThat(RoutingContextEx.getDecodedBody(context), is("expected"));
    }

    @Test
    public void getOptionalDecodedBody() {
        doReturn("expected").when(context).get(RoutingContextEx.BODY_KEY);
        assertThat(RoutingContextEx.getOptionalDecodedBody(context), is(Optional.of("expected")));
    }

    @Test
    public void getMissingOptionalDecodedBody() {
        assertThat(RoutingContextEx.getOptionalDecodedBody(context), is(Optional.empty()));
    }

    @Test
    public void geRequestBodyWhenNoBody() {
        expect(() -> RoutingContextEx.getDecodedBody(context)).toThrow(BadParamException.class);
    }

    @Test
    public void putRequestBody() {
        Object body = new Object();
        RoutingContextEx.putRequestBody(context, body);
        verify(context).put(RoutingContextEx.BODY_KEY, body);
    }

}
