/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class BodyTypeTest {

    @Test
    public void jsonObject() {
        JsonObject jsonObject = new JsonObject().put("test", "value");
        JsonObject converted = BodyType.JSON_OBJECT.convert(Buffer.buffer(jsonObject.encode()));
        assertThat(converted, equalTo(jsonObject));
    }

    @Test
    public void badJsonObjectReturnsNull() {
        expect(() -> BodyType.JSON_OBJECT.convert(Buffer.buffer("rubbish"))).toThrow(ValueConversionException.class);
    }

    @Test
    public void emptyJsonObjectReturnsNull() {
        expect(() -> BodyType.JSON_OBJECT.convert(Buffer.buffer())).toThrow(ValueConversionException.class);
    }

    @Test
    public void jsonArray() {
        JsonArray jsonArray = new JsonArray().add(1).add("a string");
        JsonArray converted = BodyType.JSON_ARRAY.convert(Buffer.buffer(jsonArray.encode()));
        assertThat(converted, equalTo(jsonArray));
    }

    @Test
    public void badJsonArrayReturnsNull() {
        expect(() -> BodyType.JSON_ARRAY.convert(Buffer.buffer("rubbish"))).toThrow(ValueConversionException.class);
    }

    @Test
    public void emptyJsonArrayReturnsNull() {
        expect(() -> BodyType.JSON_ARRAY.convert(Buffer.buffer())).toThrow(ValueConversionException.class);
    }
}
