/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.zepben.annotations.EverythingIsNonnullByDefault;

import javax.annotation.Nullable;

@EverythingIsNonnullByDefault
public class QueryParamRule<T> extends ParamRule<String, T> {

    @Nullable private final T defaultValue;
    private final boolean isRequired;

    public static <T> QueryParamRule<T> of(String name, RequestValueConverter<String, T> converter) {
        return new QueryParamRule<>(name, converter, null, false);
    }

    public static <T> QueryParamRule<T> of(String name, RequestValueConverter<String, T> converter, T defaultValue) {
        return new QueryParamRule<>(name, converter, defaultValue, false);
    }

    public static <T> QueryParamRule<T> ofRequired(String name, RequestValueConverter<String, T> converter) {
        return new QueryParamRule<>(name, converter, null, true);
    }

    private QueryParamRule(String name, RequestValueConverter<String, T> converter, @Nullable T defaultValue, boolean isRequired) {
        super(name, converter);
        this.defaultValue = defaultValue;
        this.isRequired = isRequired;
    }

    @Nullable
    public T defaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return isRequired;
    }
}
