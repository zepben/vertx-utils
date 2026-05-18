/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

class QueryParamRule<T> private constructor(
    name: String,
    converter: RequestValueConverter<String, T>,
    val defaultValue: T?,
    val isRequired: Boolean,
) : ParamRule<String, T>(name, converter) {

    companion object {

        fun <T> of(name: String, converter: RequestValueConverter<String, T>): QueryParamRule<T> =
            QueryParamRule(name, converter, null, false)

        fun <T> of(name: String, converter: RequestValueConverter<String, T>, defaultValue: T): QueryParamRule<T> =
            QueryParamRule(name, converter, defaultValue, false)

        fun <T> ofRequired(name: String, converter: RequestValueConverter<String, T>): QueryParamRule<T> =
            QueryParamRule(name, converter, null, true)
    }

}
