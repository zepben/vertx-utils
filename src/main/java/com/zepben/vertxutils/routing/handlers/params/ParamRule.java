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
public abstract class ParamRule<T, R> {

    private final String name;
    private final RequestValueConverter<T, R> converter;

    @SuppressWarnings("WeakerAccess")
    public ParamRule(String name, RequestValueConverter<T, R> converter) {
        this.name = name;
        this.converter = converter;
    }

    public String name() {
        return name;
    }

    public RequestValueConverter<T, R> converter() {
        return converter;
    }

}
