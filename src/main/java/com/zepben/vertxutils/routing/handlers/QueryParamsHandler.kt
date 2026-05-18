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
import com.zepben.vertxutils.routing.handlers.params.QueryParamRule
import com.zepben.vertxutils.routing.handlers.params.QueryParams
import com.zepben.vertxutils.routing.handlers.params.ValueConversionException
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class QueryParamsHandler(
    rules: Collection<QueryParamRule<*>>,
) : Handler<RoutingContext> {

    val rules: Map<String, QueryParamRule<*>> = rules.associateBy { it.name }

    private val rulesSet = rules.toSet()

    constructor(vararg rules: QueryParamRule<*>) : this(rules.toList())

    init {
        require(rules.size == this.rules.size) { "INTERNAL ERROR: The rules you have passed have a duplicate key." }
    }

    override fun handle(context: RoutingContext) {
        val params = mutableMapOf<String, List<Any>>()
        val errors = rules.values.mapNotNull { rule ->
            try {
                val strValues = context.queryParam(rule.name)
                val values = mutableListOf<Any>()

                if (strValues == null || strValues.isEmpty()) {
                    if (rule.isRequired)
                        throw BadParamException.missingParam(rule.name)
                } else {
                    strValues.forEach { strValue ->
                        try {
                            val value = rule.converter.convert(strValue)
                                ?: throw BadParamException.invalidParam(rule, strValue, "value was converted into null value")

                            values.add(value)
                        } catch (ex: ValueConversionException) {
                            throw BadParamException.invalidParam(rule, strValue, ex.message)
                        }
                    }
                }

                if (!values.isEmpty())
                    params[rule.name] = values

                null
            } catch (ex: BadParamException) {
                ex.message
            }
        }

        if (errors.isEmpty()) {
            RoutingContextEx.putQueryParams(context, QueryParams(rulesSet, params))
            context.next()
        } else {
            Respond.withJson(context, HttpResponseStatus.BAD_REQUEST, ErrorFormatter.asJson(errors))
        }
    }

}
