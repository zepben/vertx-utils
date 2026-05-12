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
import java.util.Objects;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;

public class RoutingContextExTest {

    private final RoutingContext context = mock(RoutingContext.class);

    @Test
    public void keysAreDifferent() {
        assertThat(RoutingContextEx.INSTANCE.getPATH_PARAMS_KEY(), not(equalTo(RoutingContextEx.INSTANCE.getQUERY_PARAMS_KEY())));
        assertThat(RoutingContextEx.INSTANCE.getPATH_PARAMS_KEY(), not(equalTo(RoutingContextEx.INSTANCE.getBODY_KEY())));
    }

    @Test
    public void getPathParams() {
        PathParams params = new PathParams(Collections.emptyMap());
        doReturn(params).when(context).get(RoutingContextEx.INSTANCE.getPATH_PARAMS_KEY());
        assertThat(RoutingContextEx.INSTANCE.getPathParams(context), is(params));
    }

    @Test
    public void getPathParamsWhenNoParams() {
        expect(() -> RoutingContextEx.INSTANCE.getPathParams(context)).toThrow(IllegalStateException.class);
    }

    @Test
    public void putPathParams() {
        PathParams params = new PathParams(Collections.emptyMap());
        RoutingContextEx.INSTANCE.putPathParams(context, params);
        verify(context).put(RoutingContextEx.INSTANCE.getPATH_PARAMS_KEY(), params);
    }

    @Test
    public void getQueryParams() {
        QueryParams params = mock(QueryParams.class);
        doReturn(params).when(context).get(RoutingContextEx.INSTANCE.getQUERY_PARAMS_KEY());
        assertThat(RoutingContextEx.INSTANCE.getQueryParams(context), is(params));
    }

    @Test
    public void getQueryParamsWhenNoParams() {
        expect(() -> RoutingContextEx.INSTANCE.getQueryParams(context)).toThrow(IllegalStateException.class);
    }

    @Test
    public void putQueryParams() {
        QueryParams params = mock(QueryParams.class);
        RoutingContextEx.INSTANCE.putQueryParams(context, params);
        verify(context).put(RoutingContextEx.INSTANCE.getQUERY_PARAMS_KEY(), params);
    }

    @Test
    public void getDecodedBody() {
        doReturn("expected").when(context).get(RoutingContextEx.INSTANCE.getBODY_KEY());
        assertThat(RoutingContextEx.INSTANCE.getDecodedBody(context), is("expected"));
    }

    @Test
    public void getOptionalDecodedBody() {
        doReturn("expected").when(context).get(RoutingContextEx.INSTANCE.getBODY_KEY());
        assertThat(Objects.requireNonNull(RoutingContextEx.INSTANCE.getOptionalDecodedBody(context)), is("expected"));
    }

    @Test
    public void getMissingOptionalDecodedBody() {
        assertThat(RoutingContextEx.INSTANCE.getOptionalDecodedBody(context), nullValue());
    }

    @Test
    public void geRequestBodyWhenNoBody() {
        expect(() -> RoutingContextEx.INSTANCE.getDecodedBody(context)).toThrow(BadParamException.class);
    }

    @Test
    public void putRequestBody() {
        Object body = new Object();
        RoutingContextEx.INSTANCE.putRequestBody(context, body);
        verify(context).put(RoutingContextEx.INSTANCE.getBODY_KEY(), body);
    }

}
