/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.mockito.Mockito.*;

public class FaviconHandlerTest {

    @TempDir
    public File temporaryFolder;

    private final Vertx vertx = Vertx.vertx();
    private FaviconHandler handler;
    private final Buffer buffer = Buffer.buffer("the icon!");
    private final RoutingContext context = mock(RoutingContext.class);
    private final HttpServerResponse response = mock(HttpServerResponse.class);

    @BeforeEach
    public void setUp() throws Exception {
        Path faviconPath = Paths.get(temporaryFolder.getPath(), "favicon.ico");
        Files.write(faviconPath, buffer.getBytes());
        handler = new FaviconHandler(faviconPath.toString(), 10);
        doReturn(response).when(context).response();
        doReturn(vertx).when(context).vertx();
    }

    @AfterEach
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void handle() {
        handler.handle(context);

        verify(response).putHeader("Content-Type", "image/x-icon");
        verify(response).putHeader("Content-Length", Integer.toString(buffer.length()));
        verify(response).putHeader("Cache-Control", "public, max-age=" + 10);
        verify(response).end(buffer);
    }

    @Test
    public void cachesIcon() throws Exception {
        handler.handle(context);
        Files.delete(Paths.get(handler.faviconPath()));
        handler.handle(context);
        verify(response, times(2)).end(buffer);
    }

    @Test
    public void maxAgeMustBePositive() {
        expect(() -> new FaviconHandler(handler.faviconPath(), -1)).toThrow(IllegalArgumentException.class);
    }
}
