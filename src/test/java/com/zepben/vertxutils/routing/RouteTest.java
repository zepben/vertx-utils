/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.vertxutils.routing.handlers.DecodeBodyHandler;
import com.zepben.vertxutils.routing.handlers.PathParamsHandler;
import com.zepben.vertxutils.routing.handlers.QueryParamsHandler;
import com.zepben.vertxutils.routing.handlers.UtilHandlers;
import com.zepben.vertxutils.routing.handlers.params.BodyType;
import com.zepben.vertxutils.routing.handlers.params.ParamType;
import com.zepben.vertxutils.routing.handlers.params.PathParamRule;
import com.zepben.vertxutils.routing.handlers.params.QueryParamRule;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

public class RouteTest {

    @Test
    public void path() {
        Route route = Route.Companion.builder().path("/a/path").build();
        assertThat(route.getPath(), is("/a/path"));
    }

    @Test
    public void formatPath() {
        PathParamRule<String> rule = PathParamRule.Companion.of("test", ParamType.INSTANCE.getSTRING());
        Route route = Route.Companion.builder().path("/a/path/:%s", rule).build();
        assertThat(route.getPath(), is("/a/path/:test"));
    }

    @Test
    public void pathMustNotBeEmpty() {
        expect(() -> Route.Companion.builder().path("")).toThrow(IllegalArgumentException.class).withMessage("path must not be empty");
    }

    @Test
    public void validatePath() {
        expect(() -> Route.Companion.builder().path("woop").build()).toThrow(IllegalStateException.class).withMessage("path must start with a /");

        // This should not throw because of regex flag
        Route.Companion.builder().path("woop").hasRegexPath(true).build();
    }

    @Test
    public void validatePathParams() {
        PathParamRule<String> rule = PathParamRule.Companion.of("test", ParamType.INSTANCE.getSTRING());

        expect(() -> Route.Companion.builder().path("/some/path", rule)).toThrow(IllegalArgumentException.class).withMessage("too many path params");
        expect(() -> Route.Companion.builder().path("/some/path/:%s/:%s", rule)).toThrow(IllegalArgumentException.class).withMessage("missing path params");
        expect(() -> Route.Companion.builder().path("/some/path/:%d", rule)).toThrow(IllegalArgumentException.class).withMessage("invalid use of % in path format string");
        expect(() -> Route.Companion.builder().path("/some/path/:%%", rule)).toThrow(IllegalArgumentException.class).withMessage("invalid use of % in path format string");
        expect(() -> Route.Companion.builder().path("/some/path/%s", rule)).toThrow(IllegalArgumentException.class).withMessage("invalid use of % in path format string");
        expect(() -> Route.Companion.builder().path("/some/path/:%s")).toThrow(IllegalArgumentException.class).withMessage("formatted path must not contain a '%'");
    }

    @Test
    public void pathParamsHandler() {
        PathParamRule<String> rule = PathParamRule.Companion.of("test", ParamType.INSTANCE.getSTRING());
        Route route = Route.Companion.builder()
            .addHandler(r -> {
            })
            .path("/a/path/:%s", rule).build();

        // Make sure the params handler is before any other registered handler.
        assertThat(route.getHandlers().get(0).getHandler(), instanceOf(PathParamsHandler.class));

        PathParamsHandler handler = (PathParamsHandler) route.getHandlers().get(0).getHandler();
        assertThat(handler.getRules().values(), containsInAnyOrder(rule));
    }

    @Test
    public void defaultRegexPath() {
        assertThat(Route.Companion.builder().build().getHasRegexPath(), is(false));
    }

    @Test
    public void setRegexPath() {
        assertThat(Route.Companion.builder().hasRegexPath(true).build().getHasRegexPath(), is(true));
    }

    @Test
    public void method() {
        assertThat(Route.Companion.builder().method(HttpMethod.GET).build().getMethods(), contains(HttpMethod.GET));
    }

    @Test
    public void methods() {
        assertThat(Route.Companion.builder().methods(HttpMethod.GET, HttpMethod.POST).build().getMethods(), contains(HttpMethod.GET, HttpMethod.POST));
    }

    @Test
    public void queryParams() {
        QueryParamRule<String> rule1 = QueryParamRule.Companion.of("p1", ParamType.INSTANCE.getSTRING());
        QueryParamRule<Integer> rule2 = QueryParamRule.Companion.of("p2", ParamType.INSTANCE.getINT());

        Route route = Route.Companion.builder()
            .addHandler(rc -> {
            })
            .queryParams(rule1, rule2)
            .build();

        // Make sure the params handler is before any other registered handler.
        assertThat(route.getHandlers().get(0).getHandler(), instanceOf(QueryParamsHandler.class));

        QueryParamsHandler handler = (QueryParamsHandler) route.getHandlers().get(0).getHandler();
        assertThat(handler.getRules().values(), containsInAnyOrder(rule1, rule2));
    }

    @Test
    public void bodySizeLimit() {
        Route route = Route.Companion.builder()
            .addHandler(rc -> {
            })
            .bodySizeLimit(1)
            .build();

        // Make sure the body handler is before any other registered handler.
        assertThat(route.getHandlers().get(0).getHandler(), instanceOf(BodyHandler.class));

        // Unfortunately there is no easy way to test the size was set correctly on the BodyHandler as it does not expose getters.
    }

    @Test
    public void uploadsDirectory() {
        Route route = Route.Companion.builder()
            .addHandler(rc -> {
            })
            .uploadsDirectory("/some/path")
            .build();

        // Make sure the body handler is before any other registered handler.
        assertThat(route.getHandlers().get(0).getHandler(), instanceOf(BodyHandler.class));

        // Unfortunately there is no easy way to test the directory was set correctly on the BodyHandler as it does not expose getters.
    }

    @Test
    public void decodeBody() {
        Route route = Route.Companion.builder()
            .addHandler(rc -> {
            })
            .decodeBody(BodyType.INSTANCE.getJSON_OBJECT(), true)
            .build();

        // Make sure the body handler is before any other registered handler.
        assertThat(route.getHandlers().get(0).getHandler(), instanceOf(BodyHandler.class));
        assertThat(route.getHandlers().get(1).getHandler(), instanceOf(DecodeBodyHandler.class));

        DecodeBodyHandler handler = (DecodeBodyHandler) route.getHandlers().get(1).getHandler();
        assertThat(handler.getBodyRule().getConverter(), is(BodyType.INSTANCE.getJSON_OBJECT()));
        assertThat(handler.getBodyRule().isRequired(), is(true));
    }

    @Test
    public void decodeBodyOptional() {
        Route route = Route.Companion.builder()
            .addHandler(rc -> {
            })
            .decodeBody(BodyType.INSTANCE.getJSON_OBJECT(), false)
            .build();

        // Make sure the body handler is before any other registered handler.
        assertThat(route.getHandlers().get(0).getHandler(), instanceOf(BodyHandler.class));
        assertThat(route.getHandlers().get(1).getHandler(), instanceOf(DecodeBodyHandler.class));

        DecodeBodyHandler handler = (DecodeBodyHandler) route.getHandlers().get(1).getHandler();
        assertThat(handler.getBodyRule().getConverter(), is(BodyType.INSTANCE.getJSON_OBJECT()));
        assertThat(handler.getBodyRule().isRequired(), is(false));
    }

    @Test
    public void failureHandler() {
        List<Handler<RoutingContext>> handlers = Route.Companion.builder()
            .addFailureHandler(UtilHandlers.INSTANCE.getCATCH_ALL_API_FAILURE_HANDLER())
            .build()
            .getFailureHandlers();

        assertThat(handlers.get(0), is(UtilHandlers.INSTANCE.getCATCH_ALL_API_FAILURE_HANDLER()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void exceptionFailureHandler() {
        Function2<RuntimeException, RoutingContext, Unit> handler = mock(Function2.class);
        List<Handler<RoutingContext>> handlers = Route.Companion.builder()
            .addFailureHandler(RuntimeException.class, handler)
            .build()
            .getFailureHandlers();

        assertThat(handlers.get(0), instanceOf(ExceptionHandler.class));
    }

    @Test
    public void nonBlockingHandler() {
        Route route = Route.Companion.builder().addHandler(c -> {
        }).build();
        assertThat(route.getHandlers().get(0).isBlocking(), is(false));
    }

    @Test
    public void flagsBlockingHandler() {
        Route route = Route.Companion.builder().addBlockingHandler(c -> {
        }).build();
        assertThat(route.getHandlers().get(0).isBlocking(), is(true));
    }

    @Test
    public void defaultIsPublic() {
        assertThat(Route.Companion.builder().build().isPublic(), is(true));
    }

}
