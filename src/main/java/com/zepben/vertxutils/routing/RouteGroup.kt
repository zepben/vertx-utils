/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

interface RouteGroup {

    val mountPath: String
    val routes: List<Route>

    companion object {

        fun create(mountPath: String, routes: List<Route>): RouteGroup =
            object : RouteGroup {
                override val mountPath: String = mountPath

                override val routes: List<Route> = routes
            }

    }

}
