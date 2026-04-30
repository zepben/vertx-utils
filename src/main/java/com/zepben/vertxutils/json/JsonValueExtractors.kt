/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.nio.file.Path
import java.nio.file.Paths

object JsonValueExtractors {

    /**
     * A wrapper for the built in `getDouble` that also supports "NaN" strings.
     */
    fun getDouble(jsonObject: JsonObject, key: String): Double? =
        try {
            jsonObject.getDouble(key)
        } catch (e: ClassCastException) {
            checkForNaN(requireNotNull(getStringStrict(jsonObject, key)), e)
        }

    /**
     * A wrapper for the built in `getDouble` that also supports "NaN" strings.
     */
    fun getDouble(jsonArray: JsonArray, pos: Int): Double? =
        try {
            jsonArray.getDouble(pos)
        } catch (e: ClassCastException) {
            checkForNaN(requireNotNull(getStringStrict(jsonArray, pos)), e)
        }

    fun getPath(json: JsonObject, key: String): Path? =
        getStringStrict(json, key)?.let { Paths.get(it) }

    /**
     * JsonObject::getString in VertX automatically converts some non-strings to strings, such as numbers.
     * This function ensures that the value in the object is actually a string.
     * @return jsonObject[key] if it's a string, null if it isn't found
     * @throws ClassCastException if jsonObject[key] is not a string
     */
    @Throws(ClassCastException::class)
    fun getStringStrict(jsonObject: JsonObject, key: String): String? =
        jsonObject.getValue(key) as String?

    /**
     * JsonObject::getString in VertX automatically converts some non-strings to strings, such as numbers.
     * This function ensures that the value in the array is actually a string.
     * @return jsonArray[pos] if it's a string, null if it isn't found
     * @throws ClassCastException if jsonArray[pos] is not a string
     */
    @Throws(ClassCastException::class)
    fun getStringStrict(jsonArray: JsonArray, pos: Int): String? =
        jsonArray.getValue(pos) as String?

    private fun checkForNaN(value: String, e: ClassCastException): Double =
        when (value) {
            "NaN" -> Double.NaN
            else -> throw e
        }

}
