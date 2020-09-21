/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.testutils.junit.SystemLogExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.mock;

public class RouteVersionUtilsTest {

    @RegisterExtension
    SystemLogExtension systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess();

    private final Route route1 = mock(Route.class);
    private final Route route2V2 = mock(Route.class);
    private final Route route2 = mock(Route.class);
    private final Route route3V1 = mock(Route.class);
    private final Route route3V2 = mock(Route.class);
    private final Route route3 = mock(Route.class);

    private final Function<AvailableRoute, Route> routeFactory = availableRoute -> {
        switch (availableRoute) {
            case ROUTE_1:
                return route1;
            case ROUTE_2_V2:
                return route2V2;
            case ROUTE_2:
                return route2;
            case ROUTE_3_V1:
                return route3V1;
            case ROUTE_3_V2:
                return route3V2;
            case ROUTE_3:
                return route3;
            default:
                return mock(Route.class);
        }
    };

    @Test
    public void createsRoutesForVersion() {
        validateVersionRoutes(0);
        validateVersionRoutes(1, route1, route2V2, route3V1);
        validateVersionRoutes(2, route1, route2V2, route3V2);
        validateVersionRoutes(3, route1, route2, route3);
        validateVersionRoutes(4, route1, route2, route3);
    }

    private void validateVersionRoutes(int version, Route... expectedRoutes) {
        if (expectedRoutes.length > 0)
            assertThat(RouteVersionUtils.forVersion(AvailableRoute.values(), version, routeFactory), contains(expectedRoutes));
        else
            assertThat(RouteVersionUtils.forVersion(AvailableRoute.values(), version, routeFactory), empty());
    }

    @EverythingIsNonnullByDefault
    private enum AvailableRoute implements VersionableRoute {
        ROUTE_1(RouteVersion.since(1)),
        ROUTE_2_V2(RouteVersion.between(1, 2)),
        ROUTE_2(RouteVersion.since(3)),
        ROUTE_3_V1(RouteVersion.between(1, 1)),
        ROUTE_3_V2(RouteVersion.between(2, 2)),
        ROUTE_3(RouteVersion.since(3));

        private final RouteVersion rv;

        AvailableRoute(RouteVersion rv) {
            this.rv = rv;
        }

        @Override
        public RouteVersion routeVersion() {
            return rv;
        }
    }

}
