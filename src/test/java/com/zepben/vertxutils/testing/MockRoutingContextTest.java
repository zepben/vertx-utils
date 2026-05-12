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

        RoutingContext context = MockRoutingContext.INSTANCE.builder()
            .pathParams(pathParams)
            .build();

        assertThat(RoutingContextEx.INSTANCE.getPathParams(context), is(pathParams));
    }

    @Test
    public void builderPathParams() {
        PathParamRule<String> param1 = PathParamRule.Companion.of("param1", ParamType.INSTANCE.getSTRING());
        PathParamRule<String> param2 = PathParamRule.Companion.of("param2", ParamType.INSTANCE.getSTRING());
        PathParamRule<Integer> param3 = PathParamRule.Companion.of("param3", ParamType.INSTANCE.getINT());

        RoutingContext context = MockRoutingContext.INSTANCE.builder()
            .pathParam(param1, "value1")
            .pathParam(param2, "value2")
            .pathParam(param3, 3)
            .build();

        PathParams pathParams = RoutingContextEx.INSTANCE.getPathParams(context);
        assertThat(pathParams.get(param1), equalTo("value1"));
        assertThat(pathParams.get(param2), equalTo("value2"));
        assertThat(pathParams.get(param3), equalTo(3));
    }

    @Test
    public void builderQueryParamsObject() {
        QueryParams queryParams = mock(QueryParams.class);

        RoutingContext context = MockRoutingContext.INSTANCE.builder()
            .queryParams(queryParams)
            .build();

        assertThat(RoutingContextEx.INSTANCE.getQueryParams(context), is(queryParams));
    }

    @Test
    public void builderQueryParams() {
        QueryParamRule<String> param1 = QueryParamRule.Companion.of("param1", ParamType.INSTANCE.getSTRING());
        QueryParamRule<String> param2 = QueryParamRule.Companion.of("param2", ParamType.INSTANCE.getSTRING(), "default");
        QueryParamRule<String> param3 = QueryParamRule.Companion.of("param3", ParamType.INSTANCE.getSTRING());
        QueryParamRule<String> param4 = QueryParamRule.Companion.of("param4", ParamType.INSTANCE.getSTRING());
        QueryParamRule<Integer> param5 = QueryParamRule.Companion.of("param5", ParamType.INSTANCE.getINT());

        RoutingContext context = MockRoutingContext.INSTANCE.builder()
            .queryParam(param1, "value1")
            .queryParam(param2)
            .queryParams(param3, param4)
            .queryParam(param5, 5)
            .build();

        QueryParams queryParams = RoutingContextEx.INSTANCE.getQueryParams(context);
        assertThat(queryParams.get(param1), equalTo("value1"));
        assertThat(queryParams.get(param2), equalTo("default"));
        assertThat(queryParams.contains(param3), equalTo(false));
        assertThat(queryParams.contains(param4), equalTo(false));
        assertThat(queryParams.get(param5), equalTo(5));
    }

    @Test
    public void builderBody() {
        Object body = new Object();

        RoutingContext context = MockRoutingContext.INSTANCE.builder()
            .decodedBody(body)
            .build();

        Object decodedBody = RoutingContextEx.INSTANCE.getDecodedBody(context);
        assertThat(decodedBody, is(body));
    }

}
