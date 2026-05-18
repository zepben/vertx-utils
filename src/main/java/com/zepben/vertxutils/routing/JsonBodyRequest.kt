/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import io.vertx.core.json.JsonObject

interface JsonBodyRequest {

    @Throws(IllegalArgumentException::class)
    fun <T> extract(json: JsonObject, key: String, valueSupplier: (json: JsonObject, key: String) -> T?): T =
        try {
            val value = valueSupplier(json, key)
            requireNotNull(value) { "Required key '$key' must be specified" }
            value
        } catch (ex: ClassCastException) {
            throw IllegalArgumentException("Error reading required key '$key'", ex)
        }

    @Throws(IllegalArgumentException::class)
    fun <T> extractList(json: JsonObject, key: String, minValues: Int, valueConverter: (json: JsonObject) -> T): List<T> {
        try {
            val values = json.getJsonArray(key)
            requireNotNull(values) { "Required key '$key' must be specified" }

            val converted = (0..<values.size()).map { i ->
                valueConverter(values.getJsonObject(i))
            }

            if (converted.size < minValues) {
                require(minValues != 1) { "Required key '$key' must have at least 1 value" }
                throw IllegalArgumentException("Required key '$key' must have at least $minValues values")
            }

            return converted
        } catch (ex: ClassCastException) {
            throw IllegalArgumentException("Error reading required key '$key'", ex)
        }
    }

}
