/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.zepben.annotations.EverythingIsNonnullByDefault;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class PathParams {

    private final Map<String, Object> params;

    public PathParams(Map<String, Object> params) {
        this.params = params;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(PathParamRule<T> rule) {
        Object value = params.get(rule.name());
        if (value == null)
            throw new IllegalArgumentException(String.format("INTERNAL ERROR: Path param %s was not registered with this route. Did you forget to register it?", rule.name()));

        return (T) value;
    }

    public <T> boolean exists(PathParamRule<T> rule) {
        return params.containsKey(rule.name());
    }

}
