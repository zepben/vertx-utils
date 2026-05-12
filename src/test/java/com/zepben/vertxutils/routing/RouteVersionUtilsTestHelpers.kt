/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing

@Deprecated("Get rid of this and use Kotlin directly.")
fun <T> getVersionRoutes(enum: Class<T>, factory: (T) -> Route?, version: Int): List<Route> where T : Enum<T>, T : VersionableRoute =
    enum.enumConstants
        .asSequence()
        .filter { version in it.routeVersion }
        .mapNotNull { factory(it) }
        .toList()
