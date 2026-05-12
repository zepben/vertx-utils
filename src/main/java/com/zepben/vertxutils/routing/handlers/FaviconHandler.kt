/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers

import com.zepben.vertxutils.routing.Respond
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.file.FileSystemException
import io.vertx.ext.web.RoutingContext

/**
 * Rework on the vertx [io.vertx.ext.web.handler.impl.FaviconHandlerImpl]:
 *
 *  * Allows you to register a favicon at any url path, not just /favicon.ico
 *  * Removes the ability to load from the classpath
 * 
 * @param filePath      file path to icon
 * @param maxAgeSeconds max age in http cache header
 */
class FaviconHandler(
    private val filePath: String,
    private val maxAgeSeconds: Long,
) : Handler<RoutingContext?> {

    private var cachedIcon: Buffer? = null

    init {
        require(maxAgeSeconds >= 0) { "maxAgeSeconds must be > 0" }
    }

    fun faviconPath(): String {
        return filePath
    }

    override fun handle(ctx: RoutingContext?) {
        // The context shouldn't ever be null in our use case.
        requireNotNull(ctx)

        val icon = cachedIcon ?: loadIcon(ctx).also { cachedIcon = it }

        if (icon.length() > 0) {
            ctx.response().putHeader("Content-Type", "image/x-icon")
            ctx.response().putHeader("Content-Length", icon.length().toString())
            ctx.response().putHeader("Cache-Control", "public, max-age=$maxAgeSeconds")
            ctx.response().end(icon)
        } else {
            Respond.with(ctx, HttpResponseStatus.NOT_FOUND)
        }
    }

    private fun loadIcon(ctx: RoutingContext): Buffer =
        try {
            ctx.vertx().fileSystem().readFileBlocking(filePath)
        } catch (_: FileSystemException) {
            Buffer.buffer()
        }

}
