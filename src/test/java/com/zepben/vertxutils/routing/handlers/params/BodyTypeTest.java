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
import io.vertx.ext.web.impl.RequestBodyImpl;
import org.junit.jupiter.api.Test;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

public class BodyTypeTest {

    RequestBodyImpl body = new RequestBodyImpl(mock());

    @Test
    public void jsonObject() {
        JsonObject jsonObject = new JsonObject().put("test", "value");
        body.setBuffer(Buffer.buffer(jsonObject.encode()));

        JsonObject converted = BodyType.INSTANCE.getJSON_OBJECT().convert(body);
        assertThat(converted, equalTo(jsonObject));
    }

    @Test
    public void badJsonObjectReturnsNull() {
        body.setBuffer(Buffer.buffer("rubbish"));

        expect(() -> BodyType.INSTANCE.getJSON_OBJECT().convert(body)).toThrow(ValueConversionException.class);
    }

    @Test
    public void emptyJsonObjectReturnsNull() {
        body.setBuffer(Buffer.buffer());

        expect(() -> BodyType.INSTANCE.getJSON_OBJECT().convert(body)).toThrow(ValueConversionException.class);
    }

    @Test
    public void jsonArray() {
        JsonArray jsonArray = new JsonArray().add(1).add("a string");
        body.setBuffer(Buffer.buffer(jsonArray.encode()));
        JsonArray converted = BodyType.INSTANCE.getJSON_ARRAY().convert(body);
        assertThat(converted, equalTo(jsonArray));
    }

    @Test
    public void badJsonArrayReturnsNull() {
        body.setBuffer(Buffer.buffer("rubbish"));
        expect(() -> BodyType.INSTANCE.getJSON_ARRAY().convert(body)).toThrow(ValueConversionException.class);
    }

    @Test
    public void emptyJsonArrayReturnsNull() {
        body.setBuffer(Buffer.buffer());
        expect(() -> BodyType.INSTANCE.getJSON_ARRAY().convert(body)).toThrow(ValueConversionException.class);
    }

}
