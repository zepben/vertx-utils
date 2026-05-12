/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

object RouteVersionUtils {

    inline fun <reified T> ((T) -> Route?).forVersion(version: Int): List<Route> where T : Enum<T>, T : VersionableRoute =
        enumValues<T>()
            .asSequence()
            .filter { version in it.routeVersion }
            .mapNotNull { this(it) }
            .toList()

}
