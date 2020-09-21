/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.example;


import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.routing.Route;
import com.zepben.vertxutils.routing.RoutingContextEx;
import com.zepben.vertxutils.routing.handlers.UtilHandlers;
import com.zepben.vertxutils.routing.handlers.params.BodyType;
import com.zepben.vertxutils.routing.handlers.params.ParamType;
import com.zepben.vertxutils.routing.handlers.params.PathParamRule;
import com.zepben.vertxutils.routing.handlers.params.QueryParamRule;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Optional;

@EverythingIsNonnullByDefault
public class GetFromFridgeRoute {

    private static class Params {
        private static final PathParamRule<String> ITEM = PathParamRule.of("item", ParamType.STRING);
        private static final QueryParamRule<Integer> AMOUNT = QueryParamRule.of("amount", ParamType.INT_POSITIVE);
    }

    public Route buildRoute() {
        // Alternative if you don't extend Route.
        // All the @Override methods in this class can disappear and you would use a builder like this:
        return Route.builder()
            .method(HttpMethod.GET)
            .path("/api/v1/fridge/:%s", Params.ITEM)
            .queryParams(Params.AMOUNT)
            .bodySizeLimit(1000)
            .decodeBody(BodyType.JSON_OBJECT, false) // Body required by default, have to specify if not required.
            .addBlockingHandler(this::someHandlerThatBlocks)
            .addHandler(this::aRegularHandler)
            .addFailureHandler(this::logFailure)
            .addFailureHandler(UtilHandlers.CATCH_ALL_API_FAILURE_HANDLER)
            .build();
    }

    private void aRegularHandler(RoutingContext context) {
        String item = RoutingContextEx.getPathParams(context).get(Params.ITEM);
        Integer amount = RoutingContextEx.getQueryParams(context).getOrElse(Params.AMOUNT, 1);
        Optional<JsonObject> body = RoutingContextEx.getOptionalDecodedBody(context);
        // If body was required you would go
        // JsonObject body = RoutingContextEx.getDecodedBody(context);

        if (body.isPresent() && body.get().containsKey("types")) {
            context.response()
                .setStatusCode(200)
                .end(String.format("You asked for %d of each %s in the %s category", amount, item, body.get().getJsonArray("types")));
        } else {
            context.response()
                .setStatusCode(200)
                .end(String.format("You asked for a %s", item));
        }
    }

    private void someHandlerThatBlocks(RoutingContext context) {
        System.out.println("Someone is using the fridge, waiting for my turn...");
        context.next();
    }

    private void logFailure(RoutingContext context) {
        System.out.println("This should be a logger!");
        context.next();
    }
}
