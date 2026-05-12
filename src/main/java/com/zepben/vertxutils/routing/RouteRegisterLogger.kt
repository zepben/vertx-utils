/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import org.slf4j.Logger

fun logRegisteredRoutes(logger: Logger): (String, Route) -> Unit = { path: String, route: Route ->
    route.methods.forEach { method -> logger.info("$method: $path") }
}
