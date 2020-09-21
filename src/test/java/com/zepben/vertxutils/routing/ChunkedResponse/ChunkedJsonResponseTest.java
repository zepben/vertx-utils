/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.ChunkedResponse;

import org.junit.jupiter.api.Test;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * All tests in this suite call end on the response in order to capture the internal buffer for checking.
 */
public class ChunkedJsonResponseTest {

    private final ChunkedJsonResponse response = new CaptureChunkedJsonResponse();

    @Test
    public void beginResponseObject() {
        response.ofObject().send(true);
        assertThat(response.toString(), equalTo("{"));

        expect(response::ofObject).toThrow(IllegalStateException.class);
        expect(response::ofArray).toThrow(IllegalStateException.class);
    }

    @Test
    public void beginResponseArray() {
        response.ofArray().send(true);
        assertThat(response.toString(), equalTo("["));

        expect(response::ofObject).toThrow(IllegalStateException.class);
        expect(response::ofArray).toThrow(IllegalStateException.class);
    }

    @Test
    public void endResponseObject() {
        ChunkedJsonResponse.JsonObject jsonObject = response.ofObject().endObject();
        assertThat(response.toString(), equalTo("{}"));

        expect(jsonObject::endObject).toThrow(IllegalStateException.class);
    }

    @Test
    public void endResponseArray() {
        ChunkedJsonResponse.JsonObject jsonObject = response.ofArray().endArray();
        assertThat(response.toString(), equalTo("[]"));

        expect(jsonObject::endObject).toThrow(IllegalStateException.class);
    }

    @Test
    public void generatesExpectedJson() {
        response
            .ofObject()
            .beginObject("o1")
            .addJson("o1k1", "0")
            .addJson("o1k2", "\"text\"")
            .beginArray("o1k3")
            .beginArray()
            .beginObject()
            .endObjectInArray()
            .endArrayInArray()
            .endArray()
            .beginArray("o1k4")
            .endArray()
            .endObject()
            .beginObject("o2")
            .beginObject("o3")
            .endObject()
            .beginArray("o2k1")
            .addArrayItem("0")
            .addArrayItem("1")
            .addArrayItem("2")
            .endArray()
            .endObject()
            .endObject();

        String expected = "{\"o1\":{\"o1k1\":0,\"o1k2\":\"text\",\"o1k3\":[[{}]],\"o1k4\":[]},\"o2\":{\"o3\":{},\"o2k1\":[0,1,2]}}";

        assertThat(response.toString(), equalTo(expected));
    }

}
