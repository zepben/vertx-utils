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
public class QueryParamsHandler implements Handler<RoutingContext> {

    private final Map<String, QueryParamRule<?>> rules;

    public QueryParamsHandler(QueryParamRule<?>... rules) {
        this(Arrays.asList(rules));
    }

    @SuppressWarnings("WeakerAccess")
    public QueryParamsHandler(Collection<QueryParamRule<?>> rules) {
        if (rules.stream().map(ParamRule::name).distinct().count() != rules.size())
            throw new IllegalArgumentException("INTERNAL ERROR: The rules you have passed have a duplicate key.");
        this.rules = rules.stream().collect(toMap(ParamRule::name, r -> r));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void handle(RoutingContext context) {
        Map<String, List<Object>> params = new HashMap<>();
        List<String> errors = new ArrayList<>();
        for (QueryParamRule<?> rule : rules.values()) {
            try {
                List<String> strValues = context.queryParam(rule.name());
                List<Object> values = new ArrayList<>();

                if (strValues == null || strValues.isEmpty()) {
                    if (rule.isRequired())
                        throw BadParamException.missingParam(rule.name());
                } else {
                    for (String strValue : strValues) {
                        try {
                            Object value = rule.converter().convert(strValue);
                            if (value == null)
                                throw BadParamException.invalidParam(rule, strValue, "value was converted into null value");

                            values.add(value);
                        } catch (ValueConversionException ex) {
                            throw BadParamException.invalidParam(rule, strValue, ex.getMessage());
                        }
                    }
                }

                if (!values.isEmpty())
                    params.put(rule.name(), values);
            } catch (BadParamException ex) {
                errors.add(ex.getMessage());
            }
        }

        if (errors.isEmpty()) {
            RoutingContextEx.putQueryParams(context, new QueryParams(new HashSet<>(rules.values()), params));
            context.next();
        } else {
            Respond.withJson(context, HttpResponseStatus.BAD_REQUEST, ErrorFormatter.asJson(errors));
        }
    }

    public Map<String, QueryParamRule<?>> rules() {
        return Collections.unmodifiableMap(rules);
    }
}
