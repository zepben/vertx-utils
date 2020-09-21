/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.BiConsumer;

import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "ThrowableNotThrown"})
public class ExceptionHandlerTest {

    private final RoutingContext context = mock(RoutingContext.class);

    @Test
    public void handlesExceptions() {
        IOException ioEx = new IOException("test");
        BiConsumer<IOException, RoutingContext> ioExHandler = mock(BiConsumer.class);
        doReturn(ioEx).when(context).failure();

        ExceptionHandler<IOException> handler = new ExceptionHandler<>(IOException.class, ioExHandler);
        handler.handle(context);

        verify(ioExHandler).accept(ioEx, context);
        verify(context, never()).next();
    }

    @Test
    public void handlesNoFailure() {
        new ExceptionHandler<>(RuntimeException.class, (t, c) -> {}).handle(context);
        verify(context).next();
    }

    @Test
    public void handlesNoMatch() {
        doReturn(new RuntimeException()).when(context).failure();
        BiConsumer<IOException, RoutingContext> ioExHandler = mock(BiConsumer.class);

        ExceptionHandler<IOException> handler = new ExceptionHandler<>(IOException.class, ioExHandler);
        handler.handle(context);

        verify(ioExHandler, never()).accept(any(), any());
        verify(context).next();
    }
}
