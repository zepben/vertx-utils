/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;

@FunctionalInterface
@EverythingIsNonnullByDefault
public interface ValueExtractor<T> {
    @Nullable
    T extract(JsonObject json, String key) throws Exception;
}

