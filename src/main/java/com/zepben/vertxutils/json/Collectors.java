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

import java.util.stream.Collector;

@EverythingIsNonnullByDefault
@SuppressWarnings("WeakerAccess")
public class Collectors {

    public static <T> Collector<T, JsonArray, JsonArray> toJsonArray() {
        return Collector.of(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

}
