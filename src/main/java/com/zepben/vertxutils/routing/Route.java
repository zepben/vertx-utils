/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.routing.handlers.DecodeBodyHandler;
import com.zepben.vertxutils.routing.handlers.PathParamsHandler;
import com.zepben.vertxutils.routing.handlers.QueryParamsHandler;
import com.zepben.vertxutils.routing.handlers.params.*;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class Route {

    @Nullable private final String path;
    private final boolean hasRegexPath;
    private final ImmutableSet<HttpMethod> methods;
    private final ImmutableList<RouteHandler> handlers;
    private final ImmutableList<Handler<RoutingContext>> failureHandlers;
    private final boolean isPublic;

    public static Builder builder() {
        return new Builder();
    }

    private Route(@Nullable String path,
                  boolean hasRegexPath,
                  ImmutableSet<HttpMethod> methods,
                  ImmutableList<RouteHandler> handlers,
                  ImmutableList<Handler<RoutingContext>> failureHandlers,
                  boolean isPublic) {
        this.path = path;
        this.hasRegexPath = hasRegexPath;
        this.methods = methods;
        this.handlers = handlers;
        this.failureHandlers = failureHandlers;
        this.isPublic = isPublic;
    }

    /**
     * The path of the route.
     *
     * @return The path of the route.
     */
    @Nullable
    public String path() {
        return path;
    }

    /**
     * Set to true if the path uses regular expressions. Defaults to false.
     *
     * @return true is the path uses regular expressions.
     */
    public boolean hasRegexPath() {
        return hasRegexPath;
    }

    /**
     * The HTTP method for the route.
     *
     * @return The HTTP method for the route.
     */
    public Iterable<HttpMethod> methods() {
        return methods;
    }

    /**
     * Return a list of handlers for this route.
     * <p>
     * Remember to always call {@link RoutingContext#next()} to chain to your next handler if you have more than one.
     *
     * @return A list of handlers for this route.
     */
    public List<RouteHandler> handlers() {
        return handlers;
    }

    /**
     * The failure handler for the route.
     *
     * @return The failure handler for the route.
     */
    public List<Handler<RoutingContext>> failureHandlers() {
        return failureHandlers;
    }

    /**
     * Indicates if the route should be documented as a public route.
     *
     * @return true if the route is a publicly documented route.
     */
    public boolean isPublic() {
        return isPublic;
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public static class Builder {
        @Nullable private String path = null;
        private boolean hasRegexPath = false;
        private final ImmutableSet.Builder<HttpMethod> methods = ImmutableSet.builder();
        @Nullable private PathParamsHandler pathParamsHandler = null;
        @Nullable private QueryParamsHandler queryParamsHandler = null;
        @Nullable private BodyHandler bodyHandler = null;
        @Nullable private DecodeBodyHandler decodeBodyHandler = null;
        private final ImmutableList.Builder<RouteHandler> handlers = ImmutableList.builder();
        private final ImmutableList.Builder<Handler<RoutingContext>> failureHandlers = ImmutableList.builder();
        private boolean isPublic = true;

        private Builder() {
        }

        public Builder path(String path) {
            if (path.isEmpty())
                throw new IllegalArgumentException("path must not be empty");

            if (path.indexOf('%') >= 0)
                throw new IllegalArgumentException("formatted path must not contain a '%'");

            this.path = path;
            return this;
        }

        public Builder path(String pathFormat, PathParamRule<?>... rules) {
            int count = 0;
            for (int index = pathFormat.indexOf('%'); index >= 0; index = pathFormat.indexOf('%', index + 1)) {
                ++count;
                if ((index == 0)
                    || (index >= pathFormat.length() - 1)
                    || (pathFormat.charAt(index - 1) != ':')
                    || (pathFormat.charAt(index + 1) != 's')) {
                    throw new IllegalArgumentException("invalid use of % in path format string");
                }
            }

            if (count < rules.length)
                throw new IllegalArgumentException("too many path params");
            else if (count > rules.length)
                throw new IllegalArgumentException("missing path params");

            path(String.format(pathFormat, Arrays.stream(rules).map(ParamRule::name).toArray()));
            pathParamsHandler = new PathParamsHandler(rules);
            return this;
        }

        public Builder hasRegexPath(boolean hasRegexPath) {
            this.hasRegexPath = hasRegexPath;
            return this;
        }

        public Builder method(HttpMethod method) {
            methods.add(method);
            return this;
        }

        public Builder methods(HttpMethod... methods) {
            for (HttpMethod method : methods)
                method(method);

            return this;
        }

        public Builder queryParams(QueryParamRule<?>... rules) {
            queryParamsHandler = new QueryParamsHandler(rules);
            return this;
        }

        public Builder bodySizeLimit(long size) {
            if (bodyHandler == null)
                bodyHandler(BodyHandler.create());

            bodyHandler.setBodyLimit(size);
            return this;
        }

        public Builder uploadsDirectory(String path) {
            if (bodyHandler == null)
                bodyHandler(BodyHandler.create());

            bodyHandler.setUploadsDirectory(path);
            return this;
        }

        public Builder decodeBody(RequestValueConverter<Buffer, ?> bodyConverter) {
            return decodeBody(bodyConverter, true);
        }

        public Builder decodeBody(RequestValueConverter<Buffer, ?> bodyConverter, boolean bodyRequired) {
            if (bodyHandler == null)
                bodyHandler(BodyHandler.create());

            decodeBodyHandler(new DecodeBodyHandler(new BodyRule<>(bodyConverter, bodyRequired)));
            return this;
        }

        public Builder bodyHandler(BodyHandler handler) {
            bodyHandler = handler;
            return this;
        }

        public Builder decodeBodyHandler(DecodeBodyHandler handler) {
            decodeBodyHandler = handler;
            return this;
        }

        public Builder addHandler(RouteHandler handler) {
            handlers.add(handler);
            return this;
        }

        public Builder addHandler(Handler<RoutingContext> handler) {
            return addHandler(new RouteHandler(handler, false));
        }

        /**
         * Registers a blocking handler.
         * This makes the handler equivalent to being registered with {@link io.vertx.ext.web.Route#blockingHandler(Handler, boolean)}.
         * on the {@link RouteRegister} however the boolean ordered flag is set by the argument given to the route register.
         *
         * @param blockingHandler The handler that contains blocking code.
         * @return This builder.
         */
        public final Builder addBlockingHandler(Handler<RoutingContext> blockingHandler) {
            return addHandler(new RouteHandler(blockingHandler, true, null));
        }

        /**
         * Registers a blocking handler.
         * This makes the handler equivalent to being registered with {@link io.vertx.ext.web.Route#blockingHandler(Handler, boolean)}
         * on the {@link RouteRegister}.
         *
         * @param blockingHandler The handler that contains blocking code.
         * @return This builder.
         */
        public final Builder addBlockingHandler(Handler<RoutingContext> blockingHandler, boolean ordered) {
            return addHandler(new RouteHandler(blockingHandler, true, ordered));
        }

        public final Builder addFailureHandler(Handler<RoutingContext> failureHandler) {
            this.failureHandlers.add(failureHandler);
            return this;
        }

        public final <T extends Throwable> Builder addFailureHandler(Class<T> throwableClass, BiConsumer<T, RoutingContext> handler) {
            failureHandlers.add(new ExceptionHandler<>(throwableClass, handler));
            return this;
        }

        public Builder isPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }

        public Route build() {
            if (path != null && !hasRegexPath && path.charAt(0) != '/')
                throw new IllegalStateException("path must start with a /");

            ImmutableList.Builder<RouteHandler> allHandlers = ImmutableList.builder();
            if (bodyHandler != null) {
                allHandlers.add(new RouteHandler(bodyHandler, false));
                if (decodeBodyHandler != null)
                    allHandlers.add(new RouteHandler(decodeBodyHandler, false));
            }

            if (pathParamsHandler != null)
                allHandlers.add(new RouteHandler(pathParamsHandler, false));

            if (queryParamsHandler != null)
                allHandlers.add(new RouteHandler(queryParamsHandler, false));

            allHandlers.addAll(handlers.build());

            return new Route(
                path,
                hasRegexPath,
                methods.build(),
                allHandlers.build(),
                failureHandlers.build(),
                isPublic);
        }
    }
}
