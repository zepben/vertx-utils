/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.chunked;

import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * All tests in this suite call end on the response in order to capture the internal buffer for checking.
 */
public class ChunkedJsonResponseTest {

    private final ChunkedJsonResponse response = new CaptureChunkedJsonResponse();

    @Test
    public void beginResponseObject() {
        response.ofObject(obj -> Unit.INSTANCE);
        assertThat(response.toString(), equalTo("{}"));

        // Shouldn't be able to start a new object while the buffer is not empty.
        expect(() -> response.ofObject(obj -> Unit.INSTANCE))
            .toThrow(IllegalStateException.class)
            .withMessage("Can't reuse a non-clean response builder");

        // Shouldn't be able to start a new array while the buffer is not empty.
        expect(() -> response.ofArray(arr -> Unit.INSTANCE))
            .toThrow(IllegalStateException.class)
            .withMessage("Can't reuse a non-clean response builder");
    }

    @Test
    public void beginResponseArray() {
        response.ofArray(obj -> Unit.INSTANCE);
        assertThat(response.toString(), equalTo("[]"));

        // Shouldn't be able to start a new object while the buffer is not empty.
        expect(() -> response.ofObject(obj -> Unit.INSTANCE))
            .toThrow(IllegalStateException.class)
            .withMessage("Can't reuse a non-clean response builder");

        // Shouldn't be able to start a new array while the buffer is not empty.
        expect(() -> response.ofArray(arr -> Unit.INSTANCE))
            .toThrow(IllegalStateException.class)
            .withMessage("Can't reuse a non-clean response builder");
    }

    @Test
    void canReuseIfBufferIsCleared() {
        var reusable = new ReuseableChunkedJsonResponse();

        reusable.ofObject(obj -> Unit.INSTANCE);
        reusable.ofObject(obj -> Unit.INSTANCE);
        reusable.ofArray(obj -> Unit.INSTANCE);
        reusable.ofArray(obj -> Unit.INSTANCE);
    }

    @Test
    public void generatesExpectedJson() {
        response.ofObject(obj -> {
            obj.obj("o1", o1 -> {
                o1.field("o1k1", 0);
                o1.field("o1k2", "text");
                o1.array("o1k3", o1k3 -> {
                    o1k3.array(arr -> {
                        arr.obj(innerObj -> Unit.INSTANCE);
                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                o1.array("o1k4", arr -> Unit.INSTANCE);
                return Unit.INSTANCE;
            });
            obj.obj("o2", o2 -> {
                o2.obj("o3", o3 -> Unit.INSTANCE);
                o2.array("o2k1", arr -> {
                    arr.item(0);
                    arr.item(1);
                    arr.item(2);
                    return Unit.INSTANCE;
                });
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });

        String expected = "{\"o1\":{\"o1k1\":0,\"o1k2\":\"text\",\"o1k3\":[[{}]],\"o1k4\":[]},\"o2\":{\"o3\":{},\"o2k1\":[0,1,2]}}";

        assertThat(response.toString(), equalTo(expected));
    }

    private static class ReuseableChunkedJsonResponse extends ChunkedJsonResponse {

        @Override
        protected void checkWrite(@NotNull StringBuilder sb, boolean force) {
        }

        @Override
        protected void onResponseCompleted(@NotNull StringBuilder sb) {
            sb.setLength(0);
        }

    }

}
