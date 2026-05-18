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
import com.zepben.vertxutils.routing.RoutingContextEx.BODY_KEY
import com.zepben.vertxutils.routing.RoutingContextEx.PATH_PARAMS_KEY
import com.zepben.vertxutils.routing.RoutingContextEx.QUERY_PARAMS_KEY
import com.zepben.vertxutils.routing.RoutingContextEx.getDecodedBody
import com.zepben.vertxutils.routing.RoutingContextEx.getOptionalDecodedBody
import com.zepben.vertxutils.routing.RoutingContextEx.getPathParams
import com.zepben.vertxutils.routing.RoutingContextEx.getQueryParams
import com.zepben.vertxutils.routing.RoutingContextEx.putPathParams
import com.zepben.vertxutils.routing.RoutingContextEx.putQueryParams
import com.zepben.vertxutils.routing.RoutingContextEx.putRequestBody
import com.zepben.vertxutils.routing.handlers.params.BadParamException
import com.zepben.vertxutils.routing.handlers.params.PathParams
import com.zepben.vertxutils.routing.handlers.params.QueryParams
import io.vertx.ext.web.RoutingContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.*

class RoutingContextExTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val context = mock<RoutingContext>()

    @Test
    fun keysAreDifferent() {
        assertThat(PATH_PARAMS_KEY, not(equalTo(QUERY_PARAMS_KEY)))
        assertThat(PATH_PARAMS_KEY, not(equalTo(BODY_KEY)))
    }

    @Test
    fun getPathParams() {
        val params = PathParams(emptyMap())
        doReturn(params).`when`(context).get<Any?>(PATH_PARAMS_KEY)
        assertThat(getPathParams(context), equalTo(params))
    }

    @Test
    fun getPathParamsWhenNoParams() {
        expect { getPathParams(context) }.toThrow<IllegalStateException>()
    }

    @Test
    fun putPathParams() {
        val params = PathParams(emptyMap())
        putPathParams(context, params)
        verify(context).put(PATH_PARAMS_KEY, params)
    }

    @Test
    fun getQueryParams() {
        val params = mock<QueryParams>()
        doReturn(params).`when`(context).get<Any?>(QUERY_PARAMS_KEY)
        assertThat(getQueryParams(context), equalTo(params))
    }

    @Test
    fun getQueryParamsWhenNoParams() {
        expect { getQueryParams(context) }.toThrow<IllegalStateException>()
    }

    @Test
    fun putQueryParams() {
        val params = mock<QueryParams>()
        putQueryParams(context, params)
        verify(context).put(QUERY_PARAMS_KEY, params)
    }

    @Test
    fun getDecodedBody() {
        doReturn("expected").`when`(context).get<Any?>(BODY_KEY)
        assertThat(getDecodedBody(context), equalTo("expected"))
    }

    @Test
    fun getOptionalDecodedBody() {
        doReturn("expected").`when`(context).get<Any?>(BODY_KEY)
        assertThat(getOptionalDecodedBody(context), equalTo("expected"))
    }

    @Test
    fun getMissingOptionalDecodedBody() {
        assertThat(getOptionalDecodedBody(context), nullValue())
    }

    @Test
    fun geRequestBodyWhenNoBody() {
        expect { getDecodedBody(context) }.toThrow<BadParamException>()
    }

    @Test
    fun putRequestBody() {
        val body = Any()
        putRequestBody(context, body)
        verify(context).put(BODY_KEY, body)
    }

}
