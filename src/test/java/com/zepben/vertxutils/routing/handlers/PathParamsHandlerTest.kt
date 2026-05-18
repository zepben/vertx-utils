/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.ErrorFormatter.asJson
import com.zepben.vertxutils.routing.RoutingContextEx.PATH_PARAMS_KEY
import com.zepben.vertxutils.routing.RoutingContextEx.getPathParams
import com.zepben.vertxutils.routing.handlers.params.*
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.sameInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class PathParamsHandlerTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val numParam = PathParamRule.of("num", ParamType.INT)
    private val num2Param = PathParamRule.of("num2", ParamType.INT)

    private val paramsCaptor = ArgumentCaptor.forClass(PathParams::class.java)
    private val response = mock<HttpServerResponse>(RETURNS_SELF)
    private val context = mock<RoutingContext>().also {
        doReturn(response).`when`(it).response()
    }

    @Test
    fun callContextNext() {
        PathParamsHandler().handle(context)
        verify(context).next()
    }

    @Test
    fun pathParam() {
        val handler = PathParamsHandler(numParam)

        doReturn("4").`when`(context).pathParam(numParam.name)
        handler.handle(context)

        verify(context).put(eq(PATH_PARAMS_KEY), paramsCaptor.capture())
        val params = paramsCaptor.getValue()
        assertThat(params[numParam], equalTo(4))
    }

    @Test
    fun getFromContext() {
        val expected = PathParams(emptyMap())
        doReturn(expected).`when`(context).get<Any?>(PATH_PARAMS_KEY)
        assertThat(getPathParams(context), sameInstance(expected))
    }

    @Test
    fun pathParamMissing() {
        val handler = PathParamsHandler(numParam)
        handler.handle(context)

        verifyBadParamResponse(BadParamException.missingParam(numParam.name))
    }

    @Test
    fun pathParamBad() {
        val handler = PathParamsHandler(numParam, num2Param)
        doReturn("not a number").`when`(context).pathParam(numParam.name)
        doReturn("true").`when`(context).pathParam(num2Param.name)

        val ex1 = captureException<ValueConversionException> { numParam.converter.convert("not a number") }
        val ex2 = captureException<ValueConversionException> { num2Param.converter.convert("true") }
        handler.handle(context)

        verifyBadParamResponse(
            BadParamException.invalidParam(numParam, "not a number", ex1.message),
            BadParamException.invalidParam(num2Param, "true", ex2.message),
        )
    }

    private inline fun <reified T : Exception> captureException(runnable: Runnable): T {
        try {
            runnable.run()
        } catch (ex: Exception) {
            return ex as T
        }

        throw AssertionError("Expected exception but none was thrown")
    }

    private fun verifyBadParamResponse(vararg e: BadParamException) {
        verify(response).statusCode = 400
        val json = asJson(e.map { it.message })
        verify(response).end(json)
        verify(context, never()).next()
    }

}
