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
import com.zepben.vertxutils.routing.handlers.params.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.*;

import static java.util.stream.Collectors.toMap;

@EverythingIsNonnullByDefault
public class PathParamsHandler implements Handler<RoutingContext> {


    private final Map<String, PathParamRule<?>> rules;

    @SuppressWarnings("WeakerAccess")
    public PathParamsHandler(PathParamRule<?>... rules) {
        this(Arrays.asList(rules));
    }

    @SuppressWarnings("WeakerAccess")
    public PathParamsHandler(Collection<PathParamRule<?>> rules) {
        this.rules = rules.stream().collect(toMap(ParamRule::name, r -> r));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void handle(RoutingContext context) {
        Map<String, Object> params = new HashMap<>();
        List<String> errors = new ArrayList<>();
        for (PathParamRule<?> rule : rules.values()) {
            try {
                String strValue = context.pathParam(rule.name());
                if (strValue == null)
                    throw BadParamException.missingParam(rule.name());

                try {
                    Object value = rule.converter().convert(strValue);
                    if (value == null)
                        throw BadParamException.invalidParam(rule, strValue, "value was converted into null value");

                    params.put(rule.name(), value);
                } catch (ValueConversionException ex) {
                    throw BadParamException.invalidParam(rule, strValue, ex.getMessage());
                }
            } catch (BadParamException ex) {
                errors.add(ex.getMessage());
            }
        }

        if (errors.isEmpty()) {
            RoutingContextEx.putPathParams(context, new PathParams(params));
            context.next();
        } else {
            Respond.withJson(context, HttpResponseStatus.BAD_REQUEST, ErrorFormatter.asJson(errors));
        }
    }

    public Map<String, PathParamRule<?>> rules() {
        return Collections.unmodifiableMap(rules);
    }
}
