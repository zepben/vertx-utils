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
import java.util.Collections;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class PathParamsHandlerTest {

    private final PathParamRule<Integer> numParam = PathParamRule.Companion.of("num", ParamType.INSTANCE.getINT());
    private final PathParamRule<Integer> num2Param = PathParamRule.Companion.of("num2", ParamType.INSTANCE.getINT());

    private final ArgumentCaptor<PathParams> paramsCaptor = ArgumentCaptor.forClass(PathParams.class);
    private final RoutingContext context = mock(RoutingContext.class);
    private final HttpServerResponse response = mock(HttpServerResponse.class, RETURNS_SELF);

    @BeforeEach
    public void setUp() {
        doReturn(response).when(context).response();
    }

    @Test
    public void callContextNext() {
        new PathParamsHandler().handle(context);
        verify(context).next();
    }

    @Test
    public void pathParam() {
        PathParamsHandler handler = new PathParamsHandler(numParam);

        doReturn("4").when(context).pathParam(numParam.getName());
        handler.handle(context);

        verify(context).put(eq(RoutingContextEx.INSTANCE.getPATH_PARAMS_KEY()), paramsCaptor.capture());
        PathParams params = paramsCaptor.getValue();
        assertThat(params.get(numParam), is(4));
    }

    @Test
    public void getFromContext() {
        PathParams expected = new PathParams(Collections.emptyMap());
        doReturn(expected).when(context).get(RoutingContextEx.INSTANCE.getPATH_PARAMS_KEY());
        assertThat(RoutingContextEx.INSTANCE.getPathParams(context), is(expected));
    }

    @Test
    public void pathParamMissing() {
        PathParamsHandler handler = new PathParamsHandler(numParam);
        handler.handle(context);

        verifyBadParamResponse(BadParamException.Companion.missingParam(numParam.getName()));
    }

    @Test
    public void pathParamBad() {
        PathParamsHandler handler = new PathParamsHandler(numParam, num2Param);
        doReturn("not a number").when(context).pathParam(numParam.getName());
        doReturn("true").when(context).pathParam(num2Param.getName());

        ValueConversionException ex1 = captureException(() -> numParam.getConverter().convert("not a number"), ValueConversionException.class);
        ValueConversionException ex2 = captureException(() -> num2Param.getConverter().convert("true"), ValueConversionException.class);
        handler.handle(context);

        verifyBadParamResponse(
            BadParamException.Companion.invalidParam(numParam, "not a number", ex1.getMessage()),
            BadParamException.Companion.invalidParam(num2Param, "true", ex2.getMessage()));
    }

    @SuppressWarnings("SameParameterValue")
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
        String json = ErrorFormatter.INSTANCE.asJson(Arrays.stream(e).map(Throwable::getMessage).collect(toList()));
        verify(response).end(json);
        verify(context, never()).next();
    }
}
