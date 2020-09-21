/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.routing.Respond;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nullable;

/**
 * Rework on the vertx {@link io.vertx.ext.web.handler.impl.FaviconHandlerImpl}:
 * <p>
 * <ul>
 * <li>Allows you to register a favicon at any url path, not just /favicon.ico</li>
 * <li>Removes the ability to load from the classpath</li>
 * </ul>
 */
@EverythingIsNonnullByDefault
public class FaviconHandler implements Handler<RoutingContext> {

    private final String filePath;
    @Nullable private Buffer icon;
    private final long maxAgeSeconds;

    /**
     * Create a new Favicon instance using a file in the file system and customizable cache period
     * <p>
     * <pre>
     * Router router = Router.router(vertx);
     * router.route().handler(FaviconHandler.create("/icons/icon.ico", 1000));
     * </pre>
     *
     * @param filePath      file path to icon
     * @param maxAgeSeconds max age in http cache header
     */
    @SuppressWarnings("WeakerAccess")
    public FaviconHandler(String filePath, long maxAgeSeconds) {
        this.filePath = filePath;
        this.maxAgeSeconds = maxAgeSeconds;
        if (maxAgeSeconds < 0) {
            throw new IllegalArgumentException("maxAgeSeconds must be > 0");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public String faviconPath() {
        return filePath;
    }

    public void handle(RoutingContext ctx) {
        if (icon == null) {
            icon = loadIcon(ctx);
        }

        if (icon.length() > 0) {
            ctx.response().putHeader("Content-Type", "image/x-icon");
            ctx.response().putHeader("Content-Length", Integer.toString(icon.length()));
            ctx.response().putHeader("Cache-Control", "public, max-age=" + maxAgeSeconds);
            ctx.response().end(icon);
        } else {
            Respond.with(ctx, HttpResponseStatus.NOT_FOUND);
        }
    }

    private Buffer loadIcon(RoutingContext ctx) {
        try {
            return ctx.vertx().fileSystem().readFileBlocking(filePath);
        } catch (FileSystemException ex) {
            return Buffer.buffer();
        }
    }
}

