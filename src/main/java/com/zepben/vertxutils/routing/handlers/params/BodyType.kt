/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RequestBody

object BodyType {

    val JSON_OBJECT: RequestValueConverter<RequestBody, JsonObject> =
        RequestValueConverter.create("json object") { body ->
            if (body.length() == 0)
                throw ValueConversionException("unable to decode empty body")

            try {
                JsonObject(body.asString())
            } catch (ex: DecodeException) {
                throw ValueConversionException(ex.message)
            }
        }

    val JSON_ARRAY: RequestValueConverter<RequestBody, JsonArray> =
        RequestValueConverter.create("json array") { body ->
            if (body.length() == 0)
                throw ValueConversionException("unable to decode empty body")

            try {
                JsonArray(body.asString())
            } catch (ex: DecodeException) {
                throw ValueConversionException(ex.message)
            }
        }

}
