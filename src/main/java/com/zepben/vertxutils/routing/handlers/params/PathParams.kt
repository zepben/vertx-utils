/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

class PathParams(
    private val params: Map<String, Any>,
) {

    operator fun <T> get(rule: PathParamRule<T>): T {
        val value = params[rule.name]

        requireNotNull(value) { "INTERNAL ERROR: Path param ${rule.name} was not registered with this route. Did you forget to register it?" }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    fun <T> exists(rule: PathParamRule<T>): Boolean = rule.name in params

}
