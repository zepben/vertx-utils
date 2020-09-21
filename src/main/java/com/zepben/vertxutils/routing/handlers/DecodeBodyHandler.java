/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.routing.ErrorFormatter;
import com.zepben.vertxutils.routing.Respond;
import com.zepben.vertxutils.routing.RoutingContextEx;
import com.zepben.vertxutils.routing.handlers.params.BadParamException;
import com.zepben.vertxutils.routing.handlers.params.BodyRule;
import com.zepben.vertxutils.routing.handlers.params.ValueConversionException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nullable;

@EverythingIsNonnullByDefault
public class DecodeBodyHandler implements Handler<RoutingContext> {

    private final BodyRule<?> bodyRule;

    public DecodeBodyHandler(BodyRule<?> bodyRule) {
        this.bodyRule = bodyRule;
    }

    @Override
    public void handle(RoutingContext context) {
        try {
            Object decodedBody = handleBody(context);
            if (decodedBody != null)
                RoutingContextEx.putRequestBody(context, decodedBody);

            context.next();
        } catch (BadParamException ex) {
            Respond.withJson(context, HttpResponseStatus.BAD_REQUEST, ErrorFormatter.asJson(ex.getMessage()));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private Object handleBody(RoutingContext context) {
        Buffer rawBody = context.getBody();
        if (rawBody == null || rawBody.length() == 0) {
            if (bodyRule.isRequired())
                throw BadParamException.missingBody();

            return null;
        }

        try {
            Object body = bodyRule.converter().convert(rawBody);
            if (body == null)
                throw BadParamException.invalidBody(bodyRule, "value was converted into null value");

            return body;
        } catch (ValueConversionException ex) {
            throw BadParamException.invalidBody(bodyRule, ex.getMessage());
        }
    }

    public BodyRule<?> bodyRule() {
        return bodyRule;
    }
}
