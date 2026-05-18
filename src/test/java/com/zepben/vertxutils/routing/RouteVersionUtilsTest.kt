/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.RouteVersion.Companion.between
import com.zepben.vertxutils.routing.RouteVersion.Companion.since
import com.zepben.vertxutils.routing.RouteVersionUtils.forVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock

class RouteVersionUtilsTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val route1 = mock<Route>()
    private val route2V2 = mock<Route>()
    private val route2 = mock<Route>()
    private val route3V1 = mock<Route>()
    private val route3V2 = mock<Route>()
    private val route3 = mock<Route>()

    private val routeFactory = { availableRoute: AvailableRoute ->
        when (availableRoute) {
            AvailableRoute.ROUTE_1 -> route1
            AvailableRoute.ROUTE_2_V2 -> route2V2
            AvailableRoute.ROUTE_2 -> route2
            AvailableRoute.ROUTE_3_V1 -> route3V1
            AvailableRoute.ROUTE_3_V2 -> route3V2
            AvailableRoute.ROUTE_3 -> route3
        }
    }

    @Test
    fun createsRoutesForVersion() {
        validateVersionRoutes(0)
        validateVersionRoutes(1, route1, route2V2, route3V1)
        validateVersionRoutes(2, route1, route2V2, route3V2)
        validateVersionRoutes(3, route1, route2, route3)
        validateVersionRoutes(4, route1, route2, route3)
    }

    private fun validateVersionRoutes(version: Int, vararg expectedRoutes: Route) {
        val routes = routeFactory.forVersion(version)
        if (expectedRoutes.isNotEmpty())
            assertThat(routes, Matchers.contains(*expectedRoutes))
        else
            assertThat(routes, empty())
    }

    private enum class AvailableRoute(
        override val routeVersion: RouteVersion,
    ) : VersionableRoute {

        ROUTE_1(since(1)),
        ROUTE_2_V2(between(1, 2)),
        ROUTE_2(since(3)),
        ROUTE_3_V1(between(1, 1)),
        ROUTE_3_V2(between(2, 2)),
        ROUTE_3(since(3))

    }

}
