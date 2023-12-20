/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.testing;

import com.zepben.vertxutils.routing.Route;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TestHttpServerTest {

    @Test
    public void canServe() {
        try (TestHttpServer server = new TestHttpServer()) {
            Route route = Route.builder().path("/").addHandler(ctx -> ctx.response().end("The response!")).build();
            int port = server.addRoute(route).listen();

            given()
                .port(port)
                .get("/")
                .then()
                .statusCode(200)
                .body(equalTo("The response!"));
        }
    }

}
