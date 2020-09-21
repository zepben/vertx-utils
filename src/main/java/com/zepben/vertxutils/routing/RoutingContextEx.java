/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;


import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.routing.handlers.DecodeBodyHandler;
import com.zepben.vertxutils.routing.handlers.PathParamsHandler;
import com.zepben.vertxutils.routing.handlers.QueryParamsHandler;
import com.zepben.vertxutils.routing.handlers.params.BadParamException;
import com.zepben.vertxutils.routing.handlers.params.PathParams;
import com.zepben.vertxutils.routing.handlers.params.QueryParams;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

/**
 * These would ideally be extension methods for {@link io.vertx.ext.web.RoutingContext} but stupid Java doesn't have them.
 */
@EverythingIsNonnullByDefault
public class RoutingContextEx {

    public static final String PATH_PARAMS_KEY = PathParamsHandler.class.getSimpleName();
    public static final String QUERY_PARAMS_KEY = QueryParamsHandler.class.getSimpleName();
    public static final String BODY_KEY = DecodeBodyHandler.class.getSimpleName();

    public static PathParams getPathParams(RoutingContext context) {
        PathParams params = context.get(PATH_PARAMS_KEY);
        if (params == null)
            throw new IllegalStateException("PathParamsHandler must be called before you can use RoutingContextEx.getPathParams");

        return params;
    }

    public static void putPathParams(RoutingContext context, PathParams params) {
        context.put(PATH_PARAMS_KEY, params);
    }

    public static QueryParams getQueryParams(RoutingContext context) {
        QueryParams params = context.get(QUERY_PARAMS_KEY);
        if (params == null)
            throw new IllegalStateException("QueryParamsHandler must be called before you can use RoutingContextEx.getQueryParams");

        return params;
    }

    public static void putQueryParams(RoutingContext context, QueryParams params) {
        context.put(QUERY_PARAMS_KEY, params);
    }

    public static <T> T getDecodedBody(RoutingContext context) {
        T body = context.get(BODY_KEY);
        if (body == null)
            throw BadParamException.missingBody();

        return body;
    }

    public static <T> Optional<T> getOptionalDecodedBody(RoutingContext context) {
        return Optional.ofNullable(context.get(BODY_KEY));
    }

    public static void putRequestBody(RoutingContext context, Object body) {
        context.put(BODY_KEY, body);
    }
}
