/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.zepben.annotations.EverythingIsNonnullByDefault;

@EverythingIsNonnullByDefault
public class PathParamRule<T> extends ParamRule<String, T> {

    public static <T> PathParamRule<T> of(String name, RequestValueConverter<String, T> converter) {
        return new PathParamRule<>(name, converter);
    }

    private PathParamRule(String name, RequestValueConverter<String, T> converter) {
        super(name, converter);
    }
}
