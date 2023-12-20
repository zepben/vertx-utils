/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.testing;

import com.zepben.vertxutils.routing.RoutingContextEx;
import com.zepben.vertxutils.routing.handlers.params.*;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class MockRoutingContextTest {

    @Test
    public void builderPathParamsObject() {
        PathParams pathParams = mock(PathParams.class);

        RoutingContext context = MockRoutingContext.builder()
            .pathParams(pathParams)
            .build();

        assertThat(RoutingContextEx.getPathParams(context), is(pathParams));
    }

    @Test
    public void builderPathParams() {
        PathParamRule<String> param1 = PathParamRule.of("param1", ParamType.STRING);
        PathParamRule<String> param2 = PathParamRule.of("param2", ParamType.STRING);
        PathParamRule<Integer> param3 = PathParamRule.of("param3", ParamType.INT);

        RoutingContext context = MockRoutingContext.builder()
            .pathParam(param1, "value1")
            .pathParam(param2, "value2")
            .pathParam(param3, 3)
            .build();

        PathParams pathParams = RoutingContextEx.getPathParams(context);
        assertThat(pathParams.get(param1), equalTo("value1"));
        assertThat(pathParams.get(param2), equalTo("value2"));
        assertThat(pathParams.get(param3), equalTo(3));
    }

    @Test
    public void builderQueryParamsObject() {
        QueryParams queryParams = mock(QueryParams.class);

        RoutingContext context = MockRoutingContext.builder()
            .queryParams(queryParams)
            .build();

        assertThat(RoutingContextEx.getQueryParams(context), is(queryParams));
    }

    @Test
    public void builderQueryParams() {
        QueryParamRule<String> param1 = QueryParamRule.of("param1", ParamType.STRING);
        QueryParamRule<String> param2 = QueryParamRule.of("param2", ParamType.STRING, "default");
        QueryParamRule<String> param3 = QueryParamRule.of("param3", ParamType.STRING);
        QueryParamRule<String> param4 = QueryParamRule.of("param4", ParamType.STRING);
        QueryParamRule<Integer> param5 = QueryParamRule.of("param5", ParamType.INT);

        RoutingContext context = MockRoutingContext.builder()
            .queryParam(param1, "value1")
            .queryParam(param2)
            .queryParams(param3, param4)
            .queryParam(param5, 5)
            .build();

        QueryParams queryParams = RoutingContextEx.getQueryParams(context);
        assertThat(queryParams.get(param1), equalTo("value1"));
        assertThat(queryParams.get(param2), equalTo("default"));
        assertThat(queryParams.exists(param3), equalTo(false));
        assertThat(queryParams.exists(param4), equalTo(false));
        assertThat(queryParams.get(param5), equalTo(5));
    }

    @Test
    public void builderBody() {
        Object body = new Object();

        RoutingContext context = MockRoutingContext.builder()
            .decodedBody(body)
            .build();

        Object decodedBody = RoutingContextEx.getDecodedBody(context);
        assertThat(decodedBody, is(body));
    }

}
