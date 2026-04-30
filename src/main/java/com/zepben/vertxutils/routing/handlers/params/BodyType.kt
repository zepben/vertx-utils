/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

object BodyType {

    val JSON_OBJECT: RequestValueConverter<Buffer, JsonObject> =
        RequestValueConverter.create("json object") { buffer ->
            if (buffer.length() == 0)
                throw ValueConversionException("unable to decode empty body")

            try {
                JsonObject(buffer.toString())
            } catch (ex: DecodeException) {
                throw ValueConversionException(ex.message)
            }
        }

    val JSON_ARRAY: RequestValueConverter<Buffer, JsonArray> =
        RequestValueConverter.create("json array") { buffer ->
            if (buffer.length() == 0)
                throw ValueConversionException("unable to decode empty body")

            try {
                JsonArray(buffer.toString())
            } catch (ex: DecodeException) {
                throw ValueConversionException(ex.message)
            }
        }

}
