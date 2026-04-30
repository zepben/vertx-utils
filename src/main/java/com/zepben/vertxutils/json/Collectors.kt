/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json

import io.vertx.core.json.JsonArray
import java.util.stream.Collector

object Collectors {

    @JvmStatic
    fun <T> toJsonArray(): Collector<T, JsonArray, JsonArray> {
        return Collector.of<T, JsonArray>(
            { JsonArray() },
            { obj, value -> obj.add(value) },
            { obj, array -> obj.addAll(array) },
        )
    }

    fun <T> Sequence<T>.toJsonArray(): JsonArray =
        JsonArray(toList())

}
