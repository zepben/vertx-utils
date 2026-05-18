/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

class PathParamRule<T> private constructor(
    name: String,
    converter: RequestValueConverter<String, T>,
) : ParamRule<String, T>(name, converter) {

    companion object {

        fun <T> of(name: String, converter: RequestValueConverter<String, T>): PathParamRule<T> =
            PathParamRule(name, converter)
    }

}
