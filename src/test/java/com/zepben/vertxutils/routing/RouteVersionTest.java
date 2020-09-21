/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.testutils.junit.SystemLogExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RouteVersionTest {

    @RegisterExtension
    SystemLogExtension systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess();

    @Test
    public void canCheckForVersion() {
        validateAvailability(RouteVersion.since(1), true, true, true, true, true, true);
        validateAvailability(RouteVersion.since(2), false, true, true, true, true, true);
        validateAvailability(RouteVersion.since(3), false, false, true, true, true, true);
        validateAvailability(RouteVersion.since(20), false, false, true, true, true, true);
        validateAvailability(RouteVersion.since(21), false, false, false, true, true, true);
        validateAvailability(RouteVersion.since(22), false, false, false, false, true, true);
        validateAvailability(RouteVersion.since(23), false, false, false, false, false, true);

        validateAvailability(RouteVersion.between(1, 1), true, false, false, false, false, false);
        validateAvailability(RouteVersion.between(1, 2), true, true, false, false, false, false);
        validateAvailability(RouteVersion.between(1, 21), true, true, true, true, false, false);
        validateAvailability(RouteVersion.between(21, 21), false, false, false, true, false, false);
        validateAvailability(RouteVersion.between(21, 22), false, false, false, true, true, false);
    }

    private void validateAvailability(RouteVersion routeVersion, boolean isInV1, boolean isInV2, boolean isInV20, boolean isInV21, boolean isInV22, boolean isInVMax) {
        assertThat(routeVersion.includes(0), equalTo(false));
        assertThat(routeVersion.includes(1), equalTo(isInV1));
        assertThat(routeVersion.includes(2), equalTo(isInV2));
        assertThat(routeVersion.includes(20), equalTo(isInV20));
        assertThat(routeVersion.includes(21), equalTo(isInV21));
        assertThat(routeVersion.includes(22), equalTo(isInV22));
        assertThat(routeVersion.includes(Integer.MAX_VALUE), equalTo(isInVMax));
    }

}
