/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

class RouteVersion private constructor(
    private val first: Int,
    private val last: Int,
) {

    operator fun contains(version: Int): Boolean = (first <= version) && (last >= version)

    companion object {

        fun since(first: Int): RouteVersion = RouteVersion(first, Int.MAX_VALUE)
        fun between(first: Int, last: Int): RouteVersion = RouteVersion(first, last)

    }

}
