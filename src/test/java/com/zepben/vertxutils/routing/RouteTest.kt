/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.Route.Companion.builder
import com.zepben.vertxutils.routing.handlers.DecodeBodyHandler
import com.zepben.vertxutils.routing.handlers.PathParamsHandler
import com.zepben.vertxutils.routing.handlers.QueryParamsHandler
import com.zepben.vertxutils.routing.handlers.UtilHandlers.CATCH_ALL_API_FAILURE_HANDLER
import com.zepben.vertxutils.routing.handlers.params.BodyType
import com.zepben.vertxutils.routing.handlers.params.ParamType
import com.zepben.vertxutils.routing.handlers.params.PathParamRule
import com.zepben.vertxutils.routing.handlers.params.QueryParamRule
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock

class RouteTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun path() {
        val route = builder().path("/a/path").build()
        assertThat(route.path, equalTo("/a/path"))
    }

    @Test
    fun formatPath() {
        val rule = PathParamRule.of("test", ParamType.STRING)
        val route = builder().path("/a/path/:%s", rule).build()
        assertThat(route.path, equalTo("/a/path/:test"))
    }

    @Test
    fun pathMustNotBeEmpty() {
        expect { builder().path("") }.toThrow<IllegalArgumentException>()
            .withMessage("path must not be empty")
    }

    @Test
    fun validatePath() {
        expect { builder().path("woop").build() }.toThrow<IllegalStateException>()
            .withMessage("path must start with a /")

        // This should not throw because of regex flag
        builder().path("woop").hasRegexPath(true).build()
    }

    @Test
    fun validatePathParams() {
        val rule = PathParamRule.of("test", ParamType.STRING)

        expect { builder().path("/some/path", rule) }.toThrow<IllegalArgumentException>()
            .withMessage("too many path params")
        expect { builder().path("/some/path/:%s/:%s", rule) }.toThrow<IllegalArgumentException>()
            .withMessage("missing path params")
        expect { builder().path("/some/path/:%d", rule) }.toThrow<IllegalArgumentException>()
            .withMessage("invalid use of % in path format string")
        expect { builder().path("/some/path/:%%", rule) }.toThrow<IllegalArgumentException>()
            .withMessage("invalid use of % in path format string")
        expect { builder().path("/some/path/%s", rule) }.toThrow<IllegalArgumentException>()
            .withMessage("invalid use of % in path format string")
        expect { builder().path("/some/path/:%s") }.toThrow<IllegalArgumentException>()
            .withMessage("formatted path must not contain a '%'")
    }

    @Test
    fun pathParamsHandler() {
        val rule = PathParamRule.of("test", ParamType.STRING)
        val route = builder()
            .addHandler { }
            .path("/a/path/:%s", rule).build()

        // Make sure the params handler is before any other registered handler.
        assertThat(
            route.handlers[0].handler,
            instanceOf(PathParamsHandler::class.java),
        )

        val handler = route.handlers[0].handler as PathParamsHandler
        assertThat(handler.rules.values, containsInAnyOrder(rule))
    }

    @Test
    fun defaultRegexPath() {
        assertThat(builder().build().hasRegexPath, equalTo(false))
    }

    @Test
    fun setRegexPath() {
        assertThat(builder().hasRegexPath(true).build().hasRegexPath, equalTo(true))
    }

    @Test
    fun method() {
        assertThat(builder().method(HttpMethod.GET).build().methods, contains(HttpMethod.GET))
    }

    @Test
    fun methods() {
        assertThat(builder().methods(HttpMethod.GET, HttpMethod.POST).build().methods, contains(HttpMethod.GET, HttpMethod.POST))
    }

    @Test
    fun queryParams() {
        val rule1 = QueryParamRule.of("p1", ParamType.STRING)
        val rule2 = QueryParamRule.of("p2", ParamType.INT)

        val route = builder()
            .addHandler { }
            .queryParams(rule1, rule2)
            .build()

        // Make sure the params handler is before any other registered handler.
        assertThat(
            route.handlers[0].handler,
            instanceOf(QueryParamsHandler::class.java),
        )

        val handler = route.handlers[0].handler as QueryParamsHandler
        assertThat(handler.rules.values, containsInAnyOrder(rule1, rule2))
    }

    @Test
    fun bodySizeLimit() {
        val route = builder()
            .addHandler { }
            .bodySizeLimit(1)
            .build()

        // Make sure the body handler is before any other registered handler.
        assertThat(
            route.handlers[0].handler,
            instanceOf(BodyHandler::class.java),
        )

        // Unfortunately there is no easy way to test the size was set correctly on the BodyHandler as it does not expose getters.
    }

    @Test
    fun uploadsDirectory() {
        val route = builder()
            .addHandler { }
            .uploadsDirectory("/some/path")
            .build()

        // Make sure the body handler is before any other registered handler.
        assertThat(
            route.handlers[0].handler,
            instanceOf(BodyHandler::class.java),
        )

        // Unfortunately there is no easy way to test the directory was set correctly on the BodyHandler as it does not expose getters.
    }

    @Test
    fun decodeBody() {
        val route = builder()
            .addHandler { }
            .decodeBody(BodyType.JSON_OBJECT, true)
            .build()

        // Make sure the body handler is before any other registered handler.
        assertThat(
            route.handlers[0].handler,
            instanceOf(BodyHandler::class.java),
        )
        assertThat(
            route.handlers[1].handler,
            instanceOf(DecodeBodyHandler::class.java),
        )

        val handler = route.handlers[1].handler as DecodeBodyHandler
        assertThat(handler.bodyRule.converter, equalTo(BodyType.JSON_OBJECT))
        assertThat(handler.bodyRule.isRequired, equalTo(true))
    }

    @Test
    fun decodeBodyOptional() {
        val route = builder()
            .addHandler { }
            .decodeBody(BodyType.JSON_OBJECT, false)
            .build()

        // Make sure the body handler is before any other registered handler.
        assertThat(
            route.handlers[0].handler,
            instanceOf(BodyHandler::class.java),
        )
        assertThat(
            route.handlers[1].handler,
            instanceOf(DecodeBodyHandler::class.java),
        )

        val handler = route.handlers[1].handler as DecodeBodyHandler
        assertThat(handler.bodyRule.converter, equalTo(BodyType.JSON_OBJECT))
        assertThat(handler.bodyRule.isRequired, equalTo(false))
    }

    @Test
    fun failureHandler() {
        val handlers = builder()
            .addFailureHandler(CATCH_ALL_API_FAILURE_HANDLER)
            .build()
            .failureHandlers

        assertThat(handlers[0], equalTo(CATCH_ALL_API_FAILURE_HANDLER))
    }

    @Test
    fun exceptionFailureHandler() {
        val handler = mock<(RuntimeException, RoutingContext?) -> Unit>()
        val handlers = builder()
            .addFailureHandler(RuntimeException::class.java, handler)
            .build()
            .failureHandlers

        assertThat(handlers[0], instanceOf(ExceptionHandler::class.java))
    }

    @Test
    fun nonBlockingHandler() {
        val route = builder().addHandler { }.build()
        assertThat(route.handlers[0].isBlocking, equalTo(false))
    }

    @Test
    fun flagsBlockingHandler() {
        val route = builder().addBlockingHandler { }.build()
        assertThat(route.handlers[0].isBlocking, equalTo(true))
    }

    @Test
    fun defaultIsPublic() {
        assertThat(builder().build().isPublic, equalTo(true))
    }

}
