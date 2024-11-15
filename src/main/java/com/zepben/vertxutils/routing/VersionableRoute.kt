/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.annotations.EverythingIsNonnullByDefault

/**
 * It's a versionable Route
 */
@EverythingIsNonnullByDefault
interface VersionableRoute {
    fun routeVersion(): RouteVersion?
}
