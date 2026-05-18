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
import com.zepben.vertxutils.routing.handlers.params.PathParamRule
import com.zepben.vertxutils.routing.handlers.params.PathParams
import com.zepben.vertxutils.routing.handlers.params.ValueConversionException
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class PathParamsHandler(
    rules: Collection<PathParamRule<*>>,
) : Handler<RoutingContext> {

    val rules: Map<String, PathParamRule<*>> = rules.associateBy { it.name }

    constructor(vararg rules: PathParamRule<*>) : this(rules.toList())

    override fun handle(context: RoutingContext) {
        val params = mutableMapOf<String, Any>()
        val errors = rules.values.mapNotNull { rule ->
            try {
                val strValue = context.pathParam(rule.name)
                    ?: throw BadParamException.missingParam(rule.name)

                try {
                    params[rule.name] = rule.converter.convert(strValue)
                        ?: throw BadParamException.invalidParam(rule, strValue, "value was converted into null value")
                    null
                } catch (ex: ValueConversionException) {
                    throw BadParamException.invalidParam(rule, strValue, ex.message)
                }
            } catch (ex: BadParamException) {
                ex.message
            }
        }

        if (errors.isEmpty()) {
            RoutingContextEx.putPathParams(context, PathParams(params))
            context.next()
        } else {
            Respond.withJson(context, HttpResponseStatus.BAD_REQUEST, ErrorFormatter.asJson(errors))
        }
    }

}
