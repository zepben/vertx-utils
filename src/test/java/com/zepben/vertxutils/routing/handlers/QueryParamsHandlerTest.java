/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers;

import com.zepben.vertxutils.routing.ErrorFormatter;
import com.zepben.vertxutils.routing.RoutingContextEx;
import com.zepben.vertxutils.routing.handlers.params.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static com.zepben.testutils.exception.ExpectException.expect;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class QueryParamsHandlerTest {

    private final QueryParamRule<String> noDefaultParam = QueryParamRule.of("noDefault", ParamType.STRING);
    private final QueryParamRule<Integer> defaultParam = QueryParamRule.of("hasDefault", ParamType.INT, 1);
    private final QueryParamRule<Boolean> requiredParam = QueryParamRule.ofRequired("required", ParamType.BOOL);

    private final ArgumentCaptor<QueryParams> paramsCaptor = ArgumentCaptor.forClass(QueryParams.class);
    private final RoutingContext context = mock(RoutingContext.class);
    private final HttpServerResponse response = mock(HttpServerResponse.class, RETURNS_SELF);

    @BeforeEach
    public void setUp() {
        doReturn(response).when(context).response();
    }

    @Test
    public void detectsDuplicateRules() {
        expect(() -> new QueryParamsHandler(requiredParam, requiredParam)).toThrow(IllegalArgumentException.class);
    }

    @Test
    public void callContextNext() {
        new QueryParamsHandler().handle(context);
        verify(context).next();
    }

    @Test
    public void queryParamNoDefault() {
        QueryParamsHandler handler = new QueryParamsHandler(noDefaultParam);
        handler.handle(context);

        verify(context).put(eq(RoutingContextEx.QUERY_PARAMS_KEY), paramsCaptor.capture());
        QueryParams params = paramsCaptor.getValue();
        assertThat(params.exists(noDefaultParam), is(false));
    }

    @Test
    public void queryParamList() {
        QueryParamsHandler handler = new QueryParamsHandler(noDefaultParam);

        List<String> rawParams = Arrays.asList("a", "b");
        doReturn(rawParams).when(context).queryParam(noDefaultParam.name());
        handler.handle(context);

        verify(context).put(eq(RoutingContextEx.QUERY_PARAMS_KEY), paramsCaptor.capture());
        QueryParams params = paramsCaptor.getValue();
        assertThat(params.getAll(noDefaultParam), is(rawParams));
    }

    @Test
    public void queryParamWithDefault() {
        QueryParamsHandler handler = new QueryParamsHandler(defaultParam);

        doReturn(emptyList()).when(context).queryParam(defaultParam.name());
        handler.handle(context);

        verify(context).put(eq(RoutingContextEx.QUERY_PARAMS_KEY), paramsCaptor.capture());
        QueryParams params = paramsCaptor.getValue();
        assertThat(params.exists(defaultParam), is(false));
        assertThat(params.get(defaultParam), is(defaultParam.defaultValue()));
    }

    @Test
    public void queryParamRequired() {
        QueryParamsHandler handler = new QueryParamsHandler(requiredParam);

        doReturn(singletonList("true")).when(context).queryParam(requiredParam.name());
        handler.handle(context);

        verify(context).put(eq(RoutingContextEx.QUERY_PARAMS_KEY), paramsCaptor.capture());
        QueryParams params = paramsCaptor.getValue();
        assertThat(params.exists(requiredParam), is(true));
        assertThat(params.get(requiredParam), is(true));
    }

    @Test
    public void queryParamRequiredMissing() {
        QueryParamsHandler handler = new QueryParamsHandler(requiredParam);
        handler.handle(context);

        verify(context, never()).put(any(), any());
        verifyBadParamResponse(BadParamException.missingParam(requiredParam.name()));
    }

    @Test
    public void queryParamBad() {
        QueryParamsHandler handler = new QueryParamsHandler(defaultParam, requiredParam);
        doReturn(singletonList("not a number")).when(context).queryParam(defaultParam.name());
        ValueConversionException ex = captureException(() -> defaultParam.converter().convert("not a number"), ValueConversionException.class);

        handler.handle(context);

        verifyBadParamResponse(
            BadParamException.invalidParam(defaultParam, "not a number", ex.getMessage()),
            BadParamException.missingParam(requiredParam.name()));
    }

    private <T extends Exception> T captureException(Runnable runnable, Class<T> expectedExType) {
        try {
            runnable.run();
        } catch (Exception ex) {
            return expectedExType.cast(ex);
        }

        throw new AssertionError("Expected exception but none was thrown");
    }

    private void verifyBadParamResponse(BadParamException... e) {
        verify(response).setStatusCode(400);
        String json = ErrorFormatter.asJson(Arrays.stream(e).map(Throwable::getMessage).collect(toList()));
        verify(response).end(json);
        verify(context, never()).next();
    }
}
