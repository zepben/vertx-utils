/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class BodyType {

    public static final RequestValueConverter<Buffer, JsonObject> JSON_OBJECT = RequestValueConverter.create(
        "json object",
        buffer -> {
            if (buffer.length() == 0)
                throw new ValueConversionException("unable to decode empty body");

            try {
                return new JsonObject(buffer.toString());
            } catch (DecodeException ex) {
                throw new ValueConversionException(ex.getMessage());
            }
        });

    public static final RequestValueConverter<Buffer, JsonArray> JSON_ARRAY = RequestValueConverter.create(
        "json array",
        buffer -> {
            if (buffer.length() == 0)
                throw new ValueConversionException("unable to decode empty body");

            try {
                return new JsonArray(buffer.toString());
            } catch (DecodeException ex) {
                throw new ValueConversionException(ex.getMessage());
            }
        });
}
