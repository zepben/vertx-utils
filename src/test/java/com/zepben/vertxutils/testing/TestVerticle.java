/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.testing;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class TestVerticle extends AbstractVerticle {

    public static void setOnStart(Runnable onStart) {
        setOnStart((promise) -> {
            onStart.run();
            promise.complete();
        });
    }

    public static void setOnStart(Consumer<Promise<Void>> onStart) {
        TestVerticle.onStart = onStart;
    }

    public static void setOnStop(Runnable onStop) {
        setOnStop((promise) -> {
            onStop.run();
            promise.complete();
        });
    }

    public static void setOnStop(Consumer<Promise<Void>> onStop) {
        TestVerticle.onStop = onStop;
    }

    private static Consumer<Promise<Void>> onStart = Promise::complete;
    private static Consumer<Promise<Void>> onStop = Promise::complete;

    @Override
    public void start(Promise<Void> startPromise) {
        onStart.accept(startPromise);
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        onStop.accept(stopPromise);
    }

}
