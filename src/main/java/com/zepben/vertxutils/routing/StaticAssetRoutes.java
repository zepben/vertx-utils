/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.routing.handlers.FaviconHandler;
import com.zepben.vertxutils.routing.handlers.UtilHandlers;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.StaticHandler;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
@EverythingIsNonnullByDefault
public class StaticAssetRoutes {

    private final String baseUrlPath;
    @Nullable private String indexPage = null;
    private final List<String> subdirs = new ArrayList<>();
    private final String filePath;
    @Nullable private String faviconUrlPath = null;
    @Nullable private String faviconFilePath = null;
    private boolean cachingEnabled = true;
    private String defaultCharacterEncoding = StandardCharsets.UTF_8.name();

    public StaticAssetRoutes(String baseUrlPath, String filePath, String... subdirs) {
        this.baseUrlPath = addTrailingSlash(baseUrlPath);

        // Make sure we do not have a trailing slash on the base file path. This is required as vertx leaves the last
        // character that matches the path (i.e. the slash) so we do not want to have a double slash.
        if (filePath.endsWith("/"))
            this.filePath = filePath.substring(0, filePath.length() - 1);
        else
            this.filePath = filePath;

        subDirs(subdirs);
    }

    public StaticAssetRoutes indexPage(String indexPage) {
        this.indexPage = indexPage;
        return this;
    }

    public StaticAssetRoutes subDirs(String... subdirs) {
        this.subdirs.addAll(Arrays.asList(subdirs));
        return this;
    }

    public StaticAssetRoutes favicon(String subUrlPath, String subFilePath) {
        this.faviconUrlPath = String.format("%s%s", baseUrlPath, subUrlPath);
        this.faviconFilePath = String.format("%s/%s", filePath, subFilePath);
        return this;
    }

    public StaticAssetRoutes cachingEnabled(boolean cachingEnabled) {
        this.cachingEnabled = cachingEnabled;
        return this;
    }

    public StaticAssetRoutes defaultCharacterEncoding(String defaultCharacterEncoding) {
        this.defaultCharacterEncoding = defaultCharacterEncoding;
        return this;
    }

    public List<Route> buildRoutes() {
        List<Route> routes = new ArrayList<>();

        indexPageRoutes(routes);
        faviconRoute(routes);
        subdirRoutes(routes);

        return routes;
    }

    private void subdirRoutes(List<Route> routes) {
        for (String subdir : subdirs) {
            routes.add(Route.builder()
                .path(baseUrlPath + subdir + "/*")
                .method(HttpMethod.GET)
                .addHandler(newStaticHandler(filePath + "/" + subdir))
                .build());
        }
    }

    private void faviconRoute(List<Route> routes) {
        if (faviconUrlPath != null && faviconFilePath != null) {
            routes.add(Route.builder()
                .path(faviconUrlPath)
                .method(HttpMethod.GET)
                .addHandler(new FaviconHandler(faviconFilePath, 86400))
                .build());
        }
    }

    private void indexPageRoutes(List<Route> routes) {
        if (indexPage != null) {
            // Vert.x has a bug where it matches URLs with and without a trailing '/' as the same route.
            // When no trailing / is on the URL, it causes issues with relative URLs in the returned html page.
            // Workaround by providing an exact regex match for a path with no / and redirecting to path with a /
            // TODO This has been raised at vertx github, but they can't decide what to do: https://github.com/vert-x3/vertx-web/issues/85
            routes.add(Route.builder()
                .path(baseUrlPath.substring(0, baseUrlPath.length() - 1) + "$")
                .hasRegexPath(true)
                .method(HttpMethod.GET)
                .addHandler(UtilHandlers.REDIRECT_NO_TRAILING_SLASH_TO_TRAILING_SLASH_HANDLER)
                .isPublic(false)
                .build());

            // Register the index page.
            routes.add(Route.builder()
                .path(baseUrlPath)
                .method(HttpMethod.GET)
                .addHandler(newStaticHandler(filePath).setIndexPage(indexPage))
                .build());
        }
    }

    private String addTrailingSlash(String str) {
        if (str.endsWith("/")) {
            return str;
        } else {
            return str + "/";
        }
    }

    private StaticHandler newStaticHandler(String path) {
        return StaticHandler.create()
            .setCachingEnabled(cachingEnabled)
            .setAllowRootFileSystemAccess(true)
            .setWebRoot(path)
            .setDefaultContentEncoding(defaultCharacterEncoding);
    }
}
