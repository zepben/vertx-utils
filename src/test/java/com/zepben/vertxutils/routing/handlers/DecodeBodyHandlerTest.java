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
import com.zepben.vertxutils.routing.handlers.params.BadParamException;
import com.zepben.vertxutils.routing.handlers.params.BodyRule;
import com.zepben.vertxutils.routing.handlers.params.BodyType;
import com.zepben.vertxutils.routing.handlers.params.ValueConversionException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class DecodeBodyHandlerTest {

    private final BodyRule<JsonObject> requiredRule = new BodyRule<>(BodyType.JSON_OBJECT, true);
    private final BodyRule<JsonObject> notRequiredRule = new BodyRule<>(BodyType.JSON_OBJECT, false);

    private final ArgumentCaptor<Object> paramsCaptor = ArgumentCaptor.forClass(Object.class);
    private final RoutingContext context = mock(RoutingContext.class);
    private final HttpServerResponse response = mock(HttpServerResponse.class, RETURNS_SELF);

    @BeforeEach
    public void setUp() {
        doReturn(response).when(context).response();
    }

    @Test
    public void callContextNext() {
        new DecodeBodyHandler(notRequiredRule).handle(context);
        verify(context).next();
    }

    @Test
    public void body() {
        DecodeBodyHandler handler = new DecodeBodyHandler(requiredRule);

        JsonObject jsonObject = new JsonObject().put("test", "value");
        doReturn(Buffer.buffer(jsonObject.encode())).when(context).getBody();

        handler.handle(context);

        verify(context).put(eq(RoutingContextEx.BODY_KEY), paramsCaptor.capture());
        Object decodedBody = paramsCaptor.getValue();
        assertThat(decodedBody, is(jsonObject));
    }

    @Test
    public void getFromContext() {
        doReturn("expected").when(context).get(RoutingContextEx.BODY_KEY);
        assertThat(RoutingContextEx.getDecodedBody(context), is("expected"));
    }

    @Test
    public void requiredBodyMissing() {
        DecodeBodyHandler handler = new DecodeBodyHandler(requiredRule);
        handler.handle(context);
        verifyBadParamResponse(BadParamException.missingBody());
    }

    @Test
    public void bodyNull() {
        DecodeBodyHandler handler = new DecodeBodyHandler(notRequiredRule);
        handler.handle(context);

        verify(context, never()).put(eq(RoutingContextEx.BODY_KEY), any());
    }

    @Test
    public void bodyEmpty() {
        doReturn(Buffer.buffer()).when(context).getBody();
        DecodeBodyHandler handler = new DecodeBodyHandler(notRequiredRule);
        handler.handle(context);

        verify(context, never()).put(eq(RoutingContextEx.BODY_KEY), any());
    }

    @Test
    public void bodyBad() {
        DecodeBodyHandler handler = new DecodeBodyHandler(requiredRule);

        Buffer buffer = Buffer.buffer("test");
        doReturn(buffer).when(context).getBody();
        handler.handle(context);

        String reason = "";
        try {
            requiredRule.converter().convert(buffer);
        } catch (ValueConversionException ex) {
            reason = ex.getMessage();
        }

        verifyBadParamResponse(BadParamException.invalidBody(requiredRule, reason));
    }

    private void verifyBadParamResponse(BadParamException e) {
        verify(response).setStatusCode(400);
        String json = ErrorFormatter.asJson(e.getMessage());
        verify(response).end(json);
        verify(context, never()).next();
    }
}
