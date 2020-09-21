/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EverythingIsNonnullByDefault
public interface JsonBodyRequest {

    default <T> T extract(JsonObject json, String key, GetValue<T> valueSupplier) throws IllegalArgumentException {
        try {
            @Nullable T value = valueSupplier.get(json, key);
            if (value == null)
                throw new IllegalArgumentException(String.format("Required key '%s' must be specified", key));
            return value;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(String.format("Error reading required key '%s'", key), ex);
        }
    }

    default <T> List<T> extractList(JsonObject json, String key, int minValues, ValueConverter<T> valueConverter) throws IllegalArgumentException {
        try {
            @Nullable JsonArray values = json.getJsonArray(key);
            if (values == null)
                throw new IllegalArgumentException(String.format("Required key '%s' must be specified", key));

            List<T> result = new ArrayList<>();
            for (int i = 0; i < values.size(); ++i)
                result.add(valueConverter.convert(values.getJsonObject(i)));

            if (result.size() < minValues) {
                if (minValues == 1)
                    throw new IllegalArgumentException(String.format("Required key '%s' must have at least 1 value", key));
                else
                    throw new IllegalArgumentException(String.format("Required key '%s' must have at least %d values", key, minValues));
            }

            return result;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException(String.format("Error reading required key '%s'", key), ex);
        }
    }

    @FunctionalInterface
    interface GetValue<T> {
        @Nullable
        T get(JsonObject json, String key);
    }

    @FunctionalInterface
    interface ValueConverter<T> {
        @Nullable
        T convert(JsonObject json);
    }

}
