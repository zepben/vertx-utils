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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RouteVersionTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun canCheckForVersion() {
        validateAvailability(since(1), isInV1 = true, isInV2 = true, isInV20 = true, isInV21 = true, isInV22 = true, isInVMax = true)
        validateAvailability(since(2), isInV2 = true, isInV20 = true, isInV21 = true, isInV22 = true, isInVMax = true)
        validateAvailability(since(3), isInV20 = true, isInV21 = true, isInV22 = true, isInVMax = true)
        validateAvailability(since(20), isInV20 = true, isInV21 = true, isInV22 = true, isInVMax = true)
        validateAvailability(since(21), isInV21 = true, isInV22 = true, isInVMax = true)
        validateAvailability(since(22), isInV22 = true, isInVMax = true)
        validateAvailability(since(23), isInVMax = true)

        validateAvailability(between(1, 1), isInV1 = true)
        validateAvailability(between(1, 2), isInV1 = true, isInV2 = true)
        validateAvailability(between(1, 21), isInV1 = true, isInV2 = true, isInV20 = true, isInV21 = true)
        validateAvailability(between(21, 21), isInV21 = true)
        validateAvailability(between(21, 22), isInV21 = true, isInV22 = true)
    }

    private fun validateAvailability(
        routeVersion: RouteVersion,
        isInV1: Boolean = false,
        isInV2: Boolean = false,
        isInV20: Boolean = false,
        isInV21: Boolean = false,
        isInV22: Boolean = false,
        isInVMax: Boolean = false,
    ) {
        assertThat(routeVersion.contains(0), equalTo(false))
        assertThat(routeVersion.contains(1), equalTo(isInV1))
        assertThat(routeVersion.contains(2), equalTo(isInV2))
        assertThat(routeVersion.contains(20), equalTo(isInV20))
        assertThat(routeVersion.contains(21), equalTo(isInV21))
        assertThat(routeVersion.contains(22), equalTo(isInV22))
        assertThat(routeVersion.contains(Int.MAX_VALUE), equalTo(isInVMax))
    }

}
