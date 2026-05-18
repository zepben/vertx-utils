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
import com.zepben.vertxutils.routing.RoutingContextEx.BODY_KEY
import com.zepben.vertxutils.routing.RoutingContextEx.getDecodedBody
import com.zepben.vertxutils.routing.handlers.params.BadParamException
import com.zepben.vertxutils.routing.handlers.params.BadParamException.Companion.invalidBody
import com.zepben.vertxutils.routing.handlers.params.BadParamException.Companion.missingBody
import com.zepben.vertxutils.routing.handlers.params.BodyRule
import com.zepben.vertxutils.routing.handlers.params.BodyType.JSON_OBJECT
import com.zepben.vertxutils.routing.handlers.params.ValueConversionException
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RequestBody
import io.vertx.ext.web.RoutingContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class DecodeBodyHandlerTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val requiredRule = BodyRule(JSON_OBJECT, true)
    private val notRequiredRule = BodyRule(JSON_OBJECT, false)

    private val paramsCaptor = ArgumentCaptor.forClass(Any::class.java)
    private val context = mock<RoutingContext>()
    private val requestBody = mock<RequestBody>(RETURNS_SELF)
    private val response = mock<HttpServerResponse>(RETURNS_SELF)

    @BeforeEach
    fun setUp() {
        doReturn(requestBody).`when`(context).body()
        doReturn("").`when`(requestBody).asString()
        doAnswer { requestBody.asString()?.length }.`when`(requestBody).length()
        doReturn(response).`when`(context).response()
    }

    @Test
    fun callContextNext() {
        DecodeBodyHandler(notRequiredRule).handle(context)
        verify(context).next()
    }

    @Test
    fun body() {
        val handler = DecodeBodyHandler(requiredRule)

        val jsonObject = JsonObject().put("test", "value")
        doReturn(jsonObject.encode()).`when`(requestBody).asString()

        handler.handle(context)

        verify(context).put(eq(BODY_KEY), paramsCaptor.capture())
        val decodedBody = paramsCaptor.getValue()
        assertThat(decodedBody, equalTo(jsonObject))
    }

    @Test
    fun getFromContext() {
        doReturn("expected").`when`(context).get<String>(BODY_KEY)
        assertThat(getDecodedBody(context), equalTo("expected"))
    }

    @Test
    fun requiredBodyMissing() {
        val handler = DecodeBodyHandler(requiredRule)
        handler.handle(context)
        verifyBadParamResponse(missingBody())
    }

    @Test
    fun bodyNull() {
        val handler = DecodeBodyHandler(notRequiredRule)
        handler.handle(context)

        verify(context, never()).put(eq(BODY_KEY), any())
    }

    @Test
    fun bodyEmpty() {
        doReturn("").`when`(requestBody).asString()
        val handler = DecodeBodyHandler(notRequiredRule)
        handler.handle(context)

        verify(context, never()).put(eq(BODY_KEY), any())
    }

    @Test
    fun bodyBad() {
        val handler = DecodeBodyHandler(requiredRule)

        doReturn("test").`when`(requestBody).asString()
        handler.handle(context)

        var reason: String? = ""
        try {
            requiredRule.converter.convert(requestBody)
        } catch (ex: ValueConversionException) {
            reason = ex.message
        }

        verifyBadParamResponse(invalidBody(requiredRule, reason))
    }

    @Test
    internal fun `body missing`() {
        val handler = DecodeBodyHandler(requiredRule)

        handler.handle(context)

        verifyBadParamResponse(missingBody())
    }

    @Test
    internal fun `body missing -1`() {
        // Real world testing revealed a missing body actually returns -1 for the length, as opposed to 0 for an empty body.
        // The value of `asString` was also `null`, rather than an empty string.
        doReturn(-1).`when`(requestBody).length()
        doReturn(null).`when`(requestBody).asString()
        val handler = DecodeBodyHandler(requiredRule)

        handler.handle(context)

        verifyBadParamResponse(missingBody())
    }

    private fun verifyBadParamResponse(e: BadParamException) {
        verify(response).statusCode = 400
        val json = asJson(e.message)
        verify(response).end(json)
        verify(context, never()).next()
    }

}
