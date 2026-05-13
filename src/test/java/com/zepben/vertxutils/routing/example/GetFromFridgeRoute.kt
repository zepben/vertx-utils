/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.example

import com.zepben.vertxutils.routing.Route
import com.zepben.vertxutils.routing.Route.Companion.builder
import com.zepben.vertxutils.routing.RoutingContextEx.getOptionalDecodedBody
import com.zepben.vertxutils.routing.RoutingContextEx.getPathParams
import com.zepben.vertxutils.routing.RoutingContextEx.getQueryParams
import com.zepben.vertxutils.routing.handlers.UtilHandlers.CATCH_ALL_API_FAILURE_HANDLER
import com.zepben.vertxutils.routing.handlers.params.BodyType.JSON_OBJECT
import com.zepben.vertxutils.routing.handlers.params.ParamType.INT_POSITIVE
import com.zepben.vertxutils.routing.handlers.params.ParamType.STRING
import com.zepben.vertxutils.routing.handlers.params.PathParamRule
import com.zepben.vertxutils.routing.handlers.params.QueryParamRule
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

class GetFromFridgeRoute {

    object Params {

        val ITEM: PathParamRule<String> = PathParamRule.of("item", STRING)
        val AMOUNT: QueryParamRule<Int> = QueryParamRule.of("amount", INT_POSITIVE)

    }

    fun buildRoute(): Route {
        return builder()
            .method(HttpMethod.GET)
            .path("/api/v1/fridge/:%s", Params.ITEM)
            .queryParams(Params.AMOUNT)
            .bodySizeLimit(1000)
            .decodeBody(JSON_OBJECT, false) // Body required by default, have to specify if not required.
            .addBlockingHandler { context -> someHandlerThatBlocks(context) }
            .addHandler { context -> aRegularHandler(context) }
            .addFailureHandler { context -> logFailure(context) }
            .addFailureHandler(CATCH_ALL_API_FAILURE_HANDLER)
            .build()
    }

    private fun aRegularHandler(context: RoutingContext?) {
        // The context shouldn't ever be null in our use case.
        requireNotNull(context)

        val item = getPathParams(context)[Params.ITEM]
        val amount: Int = getQueryParams(context).getOrElse(Params.AMOUNT, 1)!!
        val body = getOptionalDecodedBody<JsonObject>(context)

        // If body was required you would go
        // JsonObject body = RoutingContextEx.getDecodedBody(context);
        if (body != null && body.containsKey("types")) {
            context.response()
                .setStatusCode(200)
                .end(String.format("You asked for %d of each %s in the %s category", amount, item, body.getJsonArray("types")))
        } else {
            context.response()
                .setStatusCode(200)
                .end(String.format("You asked for a %s", item))
        }
    }

    private fun someHandlerThatBlocks(context: RoutingContext?) {
        println("Someone is using the fridge, waiting for my turn...")
        context?.next()
    }

    private fun logFailure(context: RoutingContext?) {
        println("This should be a logger!")
        context?.next()
    }

}
