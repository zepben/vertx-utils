/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class JsonBodyRequestTest {

    private final BlankJsonBodyRequest request = new BlankJsonBodyRequest();

    @Test
    public void extract() {
        JsonObject jsonObject = new JsonObject()
            .put("string", "value")
            .put("int", 1)
            .put("double", 2.2);

        assertThat(request.extract(jsonObject, "string", JsonObject::getString), equalTo("value"));
        assertThat(request.extract(jsonObject, "int", JsonObject::getInteger), equalTo(1));
        assertThat(request.extract(jsonObject, "double", JsonObject::getDouble), equalTo(2.2));

        expect(() -> request.extract(jsonObject, "string", JsonObject::getInteger))
            .toThrow(IllegalArgumentException.class)
            .withMessage("Error reading required key 'string'");

        expect(() -> request.extract(jsonObject, "fake", JsonObject::getValue))
            .toThrow(IllegalArgumentException.class)
            .withMessage("Required key 'fake' must be specified");
    }

    @Test
    public void extractList() {
        JsonObject jsonObject = new JsonObject()
            .put("objArray", new JsonArray()
                .add(new JsonObject()
                    .put("id", 12)
                    .put("value", "test1"))
                .add(new JsonObject()
                    .put("id", 34)
                    .put("value", "test2")))
            .put("emptyArray", new JsonArray())
            .put("double", 2.2);

        assertThat(request.extractList(jsonObject, "objArray", 2, this::fromJson),
            contains(new TestDataPair(12, "test1"), new TestDataPair(34, "test2")));

        expect(() -> request.extractList(jsonObject, "objArray", 3, this::fromJson))
            .toThrow(IllegalArgumentException.class)
            .withMessage("Required key 'objArray' must have at least 3 values");

        expect(() -> request.extractList(jsonObject, "emptyArray", 1, this::fromJson))
            .toThrow(IllegalArgumentException.class)
            .withMessage("Required key 'emptyArray' must have at least 1 value");

        expect(() -> request.extractList(jsonObject, "double", 3, this::fromJson))
            .toThrow(IllegalArgumentException.class)
            .withMessage("Error reading required key 'double'");

        expect(() -> request.extractList(jsonObject, "fake", 3, this::fromJson))
            .toThrow(IllegalArgumentException.class)
            .withMessage("Required key 'fake' must be specified");
    }

    private TestDataPair fromJson(JsonObject jsonObject) {
        return new TestDataPair(jsonObject.getInteger("id"), jsonObject.getString("value"));
    }

    private static class BlankJsonBodyRequest implements JsonBodyRequest {
    }

    private static class TestDataPair {
        final int id;
        final String value;

        TestDataPair(int id, String value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestDataPair)) return false;
            TestDataPair that = (TestDataPair) o;
            return id == that.id &&
                Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value);
        }
    }

}
