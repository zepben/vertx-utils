/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.testing;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.routing.RoutingContextEx;
import com.zepben.vertxutils.routing.handlers.params.PathParamRule;
import com.zepben.vertxutils.routing.handlers.params.PathParams;
import com.zepben.vertxutils.routing.handlers.params.QueryParamRule;
import com.zepben.vertxutils.routing.handlers.params.QueryParams;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nullable;
import java.util.*;

import static org.mockito.Mockito.*;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class MockRoutingContext {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        @Nullable private PathParams pathParams;
        private final Map<String, Object> pathParamsMap = new HashMap<>();
        @Nullable private QueryParams queryParams;
        private final Map<String, List<Object>> queryParamsMap = new HashMap<>();
        private final Set<QueryParamRule<?>> queryParamRules = new HashSet<>();
        @Nullable private Object decodedBody;

        public RoutingContext build() {
            RoutingContext context = mock(RoutingContext.class);
            HttpServerRequest request = mock(HttpServerRequest.class, RETURNS_SELF);
            HttpServerResponse response = mock(HttpServerResponse.class, RETURNS_SELF);

            doReturn(request).when(context).request();
            doReturn(response).when(context).response();

            doReturn(Objects.requireNonNullElseGet(pathParams, () -> new PathParams(pathParamsMap))).when(context).get(RoutingContextEx.PATH_PARAMS_KEY);

            doReturn(Objects.requireNonNullElseGet(queryParams, () -> new QueryParams(queryParamRules, queryParamsMap))).when(context).get(RoutingContextEx.QUERY_PARAMS_KEY);

            doReturn(decodedBody).when(context).get(RoutingContextEx.BODY_KEY);

            return context;
        }

        public Builder pathParams(PathParams params) {
            pathParams = params;
            return this;
        }

        public Builder pathParam(PathParamRule<?> rule, Object value) {
            pathParamsMap.put(rule.name(), value);
            return this;
        }

        public Builder queryParams(QueryParams params) {
            queryParams = params;
            return this;
        }

        public Builder queryParam(QueryParamRule<?> rule) {
            queryParams(rule);
            return this;
        }

        public Builder queryParams(QueryParamRule<?>... rule) {
            queryParamRules.addAll(Arrays.asList(rule));
            return this;
        }

        public Builder queryParam(QueryParamRule<?> rule, Object... values) {
            queryParam(rule);
            queryParamsMap.computeIfAbsent(rule.name(), k -> new ArrayList<>()).addAll(Arrays.asList(values));
            return this;
        }

        public Builder decodedBody(Object decodedBody) {
            this.decodedBody = decodedBody;
            return this;
        }

        private Builder() {
        }

    }

    private MockRoutingContext() {
    }

}
