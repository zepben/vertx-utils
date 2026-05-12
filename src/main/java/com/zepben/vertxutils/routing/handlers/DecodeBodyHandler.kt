/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers

import com.zepben.vertxutils.routing.ErrorFormatter
import com.zepben.vertxutils.routing.Respond
import com.zepben.vertxutils.routing.RoutingContextEx
import com.zepben.vertxutils.routing.handlers.params.BadParamException
import com.zepben.vertxutils.routing.handlers.params.BodyRule
import com.zepben.vertxutils.routing.handlers.params.ValueConversionException
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class DecodeBodyHandler(
    val bodyRule: BodyRule<*>,
) : Handler<RoutingContext?> {

    override fun handle(context: RoutingContext?) {
        // The context shouldn't ever be null in our use case.
        requireNotNull(context)

        try {
            handleBody(context)?.also {
                RoutingContextEx.putRequestBody(context, it)
            }

            context.next()
        } catch (ex: BadParamException) {
            Respond.withJson(context, HttpResponseStatus.BAD_REQUEST, ErrorFormatter.asJson(ex.message))
        }
    }

    private fun handleBody(context: RoutingContext): Any? {
        val rawBody = context.body()
        if (rawBody == null || rawBody.length() == 0) {
            if (bodyRule.isRequired)
                throw BadParamException.missingBody()

            return null
        }

        return try {
            bodyRule.converter.convert(rawBody)
                ?: throw BadParamException.invalidBody(bodyRule, "value was converted into null value")
        } catch (ex: ValueConversionException) {
            throw BadParamException.invalidBody(bodyRule, ex.message)
        }
    }

}
