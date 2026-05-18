/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.StaticAssetsRouteConfig.Companion.of
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class StaticAssetsRouteConfigTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun accessors() {
        var config = of("root1", true)
        assertThat(config.webRoot, equalTo("root1"))
        assertThat(config.isCaching, equalTo(true))

        config = of("root2", false)
        assertThat(config.webRoot, equalTo("root2"))
        assertThat(config.isCaching, equalTo(false))
    }

}
