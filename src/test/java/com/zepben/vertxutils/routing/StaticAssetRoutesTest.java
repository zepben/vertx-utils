/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

public class StaticAssetRoutesTest {

    @TempDir
    public File temporaryFolder;

    private String filePath;
    private final String baseUrl = "/app";
    private final int port = 34251;
    private StaticAssetRoutes staticRoutes;
    private final Vertx vertx = Vertx.vertx();

    @BeforeEach
    public void setUp() throws Exception {
        RestAssured.port = port;
        filePath = Files.createDirectory(Paths.get(temporaryFolder.toString(), "webroot")).toString();
        staticRoutes = new StaticAssetRoutes(baseUrl, filePath);
    }

    @AfterEach
    public void tearDown() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        vertx.close(r -> latch.countDown());
        latch.await();
    }

    @Test
    public void indexPage() throws Exception {
        String html = "<html>The page</html>";
        Files.write(Paths.get(filePath, "index.html"), html.getBytes(StandardCharsets.UTF_8));

        staticRoutes.indexPage("index.html");
        List<Route> routes = staticRoutes.buildRoutes();
        assertThat(routes.size(), is(2));
        startServer(routes);

        given()
            .when()
            .redirects()
            .follow(false)
            .get(baseUrl)
            .then()
            .statusCode(301)
            .header("Location", baseUrl + "/");

        given()
            .when()
            .get(baseUrl + "/")
            .then()
            .statusCode(200)
            .header("Content-Type", "text/html;charset=UTF-8")
            .body(equalTo(html));
    }

    @Test
    public void favicon() throws Exception {
        String ico = "This should be an image!";
        Files.write(Paths.get(filePath, "favicon.ico"), ico.getBytes(StandardCharsets.UTF_8));

        staticRoutes.favicon("favicon.ico", "favicon.ico");
        List<Route> routes = staticRoutes.buildRoutes();
        assertThat(routes.size(), is(1));
        startServer(routes);

        given()
            .when()
            .get(baseUrl + "/favicon.ico")
            .then()
            .statusCode(200)
            .body(equalTo(ico));
    }

    @Test
    public void subdirs() throws Exception {
        String js = "const aVar = 1";
        Path jsDir = Paths.get(filePath, "js");
        Files.createDirectory(jsDir);
        Files.write(jsDir.resolve("somejs.js"), js.getBytes(StandardCharsets.UTF_8));

        staticRoutes.subDirs("js");
        List<Route> routes = staticRoutes.buildRoutes();
        assertThat(routes.size(), is(1));
        startServer(routes);

        given()
            .when()
            .get(baseUrl + "/js/somejs.js")
            .then()
            .statusCode(200)
            .body(equalTo(js));
    }

    private void startServer(Iterable<Route> routes) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Router router = Router.router(vertx);
        vertx.createHttpServer()
            .requestHandler(new RouteRegister(router, true).add(routes).router())
            .listen(port, r -> {
                latch.countDown();
                if (r.failed())
                    throw new RuntimeException("Failed to start server");
            });

        latch.await();
    }
}
