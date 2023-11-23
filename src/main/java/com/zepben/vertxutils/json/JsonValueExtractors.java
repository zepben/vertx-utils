/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@EverythingIsNonnullByDefault
@SuppressWarnings("WeakerAccess")
public class JsonValueExtractors {

    @Nullable
    public static Double getDouble(JsonObject jsonObject, String key) {
        try {
            return jsonObject.getDouble(key);
        } catch (ClassCastException e) {
            return checkForNaN(Objects.requireNonNull(getStringStrict(jsonObject, key)), e);
        }
    }

    @Nullable
    public static Double getDouble(JsonArray jsonArray, int index) {
        try {
            return jsonArray.getDouble(index);
        } catch (ClassCastException e) {
            return checkForNaN(Objects.requireNonNull(getStringStrict(jsonArray, index)), e);
        }
    }

    @Nullable
    public static Path getPath(JsonObject json, String key) {
        String string = getStringStrict(json, key);
        if (string != null)
            return Paths.get(string);
        else
            return null;
    }

    /**
     * JsonObject::getString in VertX automatically converts some non-strings to strings, such as numbers.
     * This function ensures that the value in the object is actually a string.
     * @return jsonObject[key] if it's a string, null if it isn't found
     * @throws ClassCastException if jsonObject[key] is not a string
     */
    @Nullable
    public static String getStringStrict(JsonObject jsonObject, String key) throws ClassCastException {
        return (String) jsonObject.getValue(key);
    }

    /**
     * JsonObject::getString in VertX automatically converts some non-strings to strings, such as numbers.
     * This function ensures that the value in the array is actually a string.
     * @return jsonArray[index] if it's a string, null if it isn't found
     * @throws ClassCastException if jsonArray[index] is not a string
     */
    @Nullable
    public static String getStringStrict(JsonArray jsonArray, int index) throws ClassCastException {
        return (String) jsonArray.getValue(index);
    }

    private static Double checkForNaN(String value, ClassCastException e) {
        if (value.equals("NaN"))
            return Double.NaN;
        else
            throw e;
    }
}
