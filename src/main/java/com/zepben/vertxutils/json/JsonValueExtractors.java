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

@EverythingIsNonnullByDefault
@SuppressWarnings("WeakerAccess")
public class JsonValueExtractors {

    @Nullable
    public static Double getDouble(JsonObject jsonObject, String key) {
        try {
            return jsonObject.getDouble(key);
        } catch (ClassCastException e) {
            return checkForNaN(jsonObject.getString(key), e);
        }
    }

    @Nullable
    public static Double getDouble(JsonArray jsonArray, int index) {
        try {
            return jsonArray.getDouble(index);
        } catch (ClassCastException e) {
            return checkForNaN(jsonArray.getString(index), e);
        }
    }

    @Nullable
    public static Path getPath(JsonObject json, String key) {
        String string = json.getString(key);
        if (string != null)
            return Paths.get(string);
        else
            return null;
    }

    private static Double checkForNaN(String value, ClassCastException e) {
        if (value.equals("NaN"))
            return Double.NaN;
        else
            throw e;
    }

}
