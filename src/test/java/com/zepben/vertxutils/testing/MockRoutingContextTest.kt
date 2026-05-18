/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.testing

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.RoutingContextEx.getDecodedBody
import com.zepben.vertxutils.routing.RoutingContextEx.getPathParams
import com.zepben.vertxutils.routing.RoutingContextEx.getQueryParams
import com.zepben.vertxutils.routing.handlers.params.*
import com.zepben.vertxutils.testing.MockRoutingContext.builder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock

class MockRoutingContextTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun builderPathParamsObject() {
        val pathParams = mock<PathParams>()

        val context = builder()
            .pathParams(pathParams)
            .build()

        assertThat(getPathParams(context), equalTo(pathParams))
    }

    @Test
    fun builderPathParams() {
        val param1 = PathParamRule.of("param1", ParamType.STRING)
        val param2 = PathParamRule.of("param2", ParamType.STRING)
        val param3 = PathParamRule.of("param3", ParamType.INT)

        val context = builder()
            .pathParam(param1, "value1")
            .pathParam(param2, "value2")
            .pathParam(param3, 3)
            .build()

        val pathParams = getPathParams(context)
        assertThat(pathParams[param1], equalTo("value1"))
        assertThat(pathParams[param2], equalTo("value2"))
        assertThat(pathParams[param3], equalTo(3))
    }

    @Test
    fun builderQueryParamsObject() {
        val queryParams = mock(QueryParams::class.java)

        val context = builder()
            .queryParams(queryParams)
            .build()

        assertThat(getQueryParams(context), equalTo(queryParams))
    }

    @Test
    fun builderQueryParams() {
        val param1 = QueryParamRule.of("param1", ParamType.STRING)
        val param2 = QueryParamRule.of("param2", ParamType.STRING, "default")
        val param3 = QueryParamRule.of("param3", ParamType.STRING)
        val param4 = QueryParamRule.of("param4", ParamType.STRING)
        val param5 = QueryParamRule.of("param5", ParamType.INT)

        val context = builder()
            .queryParam(param1, "value1")
            .queryParam(param2)
            .queryParams(param3, param4)
            .queryParam(param5, 5)
            .build()

        val queryParams = getQueryParams(context)
        assertThat(queryParams[param1], equalTo("value1"))
        assertThat(queryParams[param2], equalTo("default"))
        assertThat(queryParams.contains(param3), equalTo(false))
        assertThat(queryParams.contains(param4), equalTo(false))
        assertThat(queryParams[param5], equalTo(5))
    }

    @Test
    fun builderBody() {
        val body = Any()

        val context = builder()
            .decodedBody(body)
            .build()

        val decodedBody = getDecodedBody<Any?>(context)
        assertThat(decodedBody, equalTo(body))
    }

}
