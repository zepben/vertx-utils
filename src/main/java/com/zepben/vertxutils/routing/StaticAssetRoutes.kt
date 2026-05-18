/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.vertxutils.routing.handlers.FaviconHandler
import com.zepben.vertxutils.routing.handlers.UtilHandlers
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.handler.FileSystemAccess
import io.vertx.ext.web.handler.StaticHandler
import java.nio.charset.StandardCharsets

class StaticAssetRoutes(
    baseUrlPath: String,
    filePath: String,
    vararg subDirs: String,
) {

    private val baseUrlPath: String
    private var indexPage: String? = null
    private val subDirs = mutableListOf<String>()
    private val filePath: String
    private var faviconUrlPath: String? = null
    private var faviconFilePath: String? = null
    private var cachingEnabled = true
    private var defaultCharacterEncoding: String = StandardCharsets.UTF_8.name()

    init {
        this.baseUrlPath = addTrailingSlash(baseUrlPath)

        // Make sure we do not have a trailing slash on the base file path. This is required as vertx leaves the last
        // character that matches the path (i.e. the slash) so we do not want to have a double slash.
        this.filePath = if (filePath.endsWith("/"))
            filePath.substring(0, filePath.length - 1)
        else
            filePath

        subDirs(*subDirs)
    }

    fun indexPage(indexPage: String): StaticAssetRoutes = also {
        it.indexPage = indexPage
    }

    fun subDirs(vararg subDirs: String): StaticAssetRoutes = also {
        it.subDirs.addAll(subDirs)
    }

    fun favicon(subUrlPath: String, subFilePath: String): StaticAssetRoutes = apply {
        faviconUrlPath = String.format("%s%s", baseUrlPath, subUrlPath)
        faviconFilePath = String.format("%s/%s", filePath, subFilePath)
    }

    fun cachingEnabled(cachingEnabled: Boolean): StaticAssetRoutes = also {
        it.cachingEnabled = cachingEnabled
    }

    fun defaultCharacterEncoding(defaultCharacterEncoding: String): StaticAssetRoutes = also {
        it.defaultCharacterEncoding = defaultCharacterEncoding
    }

    fun buildRoutes(): List<Route> = buildList {
        addIndexPageRoutes()
        addFaviconRoute()
        addSubdirRoutes()
    }

    private fun MutableList<Route>.addSubdirRoutes() {
        for (subdir in subDirs) {
            add(
                Route.builder()
                    .path("$baseUrlPath$subdir/*")
                    .method(HttpMethod.GET)
                    .addHandler(newStaticHandler("$filePath/$subdir"))
                    .build(),
            )
        }
    }

    private fun MutableList<Route>.addFaviconRoute() {
        if (faviconUrlPath != null && faviconFilePath != null) {
            add(
                Route.builder()
                    .path(faviconUrlPath!!)
                    .method(HttpMethod.GET)
                    .addHandler(FaviconHandler(faviconFilePath!!, 86400))
                    .build(),
            )
        }
    }

    private fun MutableList<Route>.addIndexPageRoutes() {
        if (indexPage != null) {
            // Vert.x has a bug where it matches URLs with and without a trailing '/' as the same route.
            // When no trailing / is on the URL, it causes issues with relative URLs in the returned html page.
            // Workaround by providing an exact regex match for a path with no / and redirecting to path with a /
            // TODO This has been raised at vertx github, but they can't decide what to do: https://github.com/vert-x3/vertx-web/issues/85
            add(
                Route.builder()
                    .path(baseUrlPath.substring(0, baseUrlPath.length - 1) + "$")
                    .hasRegexPath(true)
                    .method(HttpMethod.GET)
                    .addHandler(UtilHandlers.REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER)
                    .isPublic(false)
                    .build(),
            )

            // Register the index page.
            add(
                Route.builder()
                    .path(baseUrlPath)
                    .method(HttpMethod.GET)
                    .addHandler(newStaticHandler(filePath).setIndexPage(indexPage))
                    .build(),
            )
        }
    }

    private fun addTrailingSlash(str: String): String =
        when {
            str.endsWith("/") -> str
            else -> "$str/"
        }

    private fun newStaticHandler(path: String): StaticHandler {
        return StaticHandler.create(FileSystemAccess.ROOT, path)
            .setCachingEnabled(cachingEnabled)
            .setDefaultContentEncoding(defaultCharacterEncoding)
    }

}
