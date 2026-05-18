/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.ErrorFormatter.asJson
import com.zepben.vertxutils.routing.RoutingContextEx.QUERY_PARAMS_KEY
import com.zepben.vertxutils.routing.handlers.params.*
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class QueryParamsHandlerTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val noDefaultParam = QueryParamRule.of("noDefault", ParamType.STRING)
    private val defaultParam = QueryParamRule.of("hasDefault", ParamType.INT, 1)
    private val requiredParam = QueryParamRule.ofRequired("required", ParamType.BOOL)

    private val paramsCaptor = ArgumentCaptor.forClass(QueryParams::class.java)
    private val response = mock<HttpServerResponse>(RETURNS_SELF)
    private val context = mock<RoutingContext>().also {
        doReturn(response).`when`(it).response()
    }

    @Test
    fun detectsDuplicateRules() {
        expect { QueryParamsHandler(requiredParam, requiredParam) }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun callContextNext() {
        QueryParamsHandler().handle(context)
        verify(context).next()
    }

    @Test
    fun queryParamNoDefault() {
        val handler = QueryParamsHandler(noDefaultParam)
        handler.handle(context)

        verify(context).put(eq(QUERY_PARAMS_KEY), paramsCaptor.capture())
        val params = paramsCaptor.getValue()
        assertThat(params.contains(noDefaultParam), equalTo(false))
    }

    @Test
    fun queryParamList() {
        val handler = QueryParamsHandler(noDefaultParam)

        val rawParams = mutableListOf("a", "b")
        doReturn(rawParams).`when`(context).queryParam(noDefaultParam.name)
        handler.handle(context)

        verify(context).put(eq(QUERY_PARAMS_KEY), paramsCaptor.capture())
        val params = paramsCaptor.getValue()
        assertThat(params.getAll(noDefaultParam), equalTo(rawParams))
    }

    @Test
    fun queryParamWithDefault() {
        val handler = QueryParamsHandler(defaultParam)

        doReturn(listOf<String>()).`when`(context).queryParam(defaultParam.name)
        handler.handle(context)

        verify(context).put(eq(QUERY_PARAMS_KEY), paramsCaptor.capture())
        val params = paramsCaptor.getValue()
        assertThat(params.contains(defaultParam), equalTo(false))
        assertThat(params[defaultParam], equalTo(defaultParam.defaultValue))
    }

    @Test
    fun queryParamRequired() {
        val handler = QueryParamsHandler(requiredParam)

        doReturn(mutableListOf("true")).`when`(context).queryParam(requiredParam.name)
        handler.handle(context)

        verify(context).put(eq(QUERY_PARAMS_KEY), paramsCaptor.capture())
        val params = paramsCaptor.getValue()
        assertThat(params.contains(requiredParam), equalTo(true))
        assertThat(params[requiredParam], equalTo(true))
    }

    @Test
    fun queryParamRequiredMissing() {
        val handler = QueryParamsHandler(requiredParam)
        handler.handle(context)

        verify(context, never()).put(any(), any())
        verifyBadParamResponse(BadParamException.missingParam(requiredParam.name))
    }

    @Test
    fun queryParamBad() {
        val handler = QueryParamsHandler(defaultParam, requiredParam)
        doReturn(mutableListOf("not a number")).`when`(context).queryParam(defaultParam.name)
        val ex = captureException<ValueConversionException> { defaultParam.converter.convert("not a number") }

        handler.handle(context)

        verifyBadParamResponse(
            BadParamException.invalidParam(defaultParam, "not a number", ex.message),
            BadParamException.missingParam(requiredParam.name),
        )
    }

    private inline fun <reified T : Exception?> captureException(runnable: Runnable): T {
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
