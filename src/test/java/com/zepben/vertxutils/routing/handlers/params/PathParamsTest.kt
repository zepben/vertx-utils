/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.handlers.params.ParamType.INT
import com.zepben.vertxutils.routing.handlers.params.ParamType.STRING
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class PathParamsTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val p1 = PathParamRule.of("p1", STRING)
    private val p2 = PathParamRule.of("p2", INT)
    private val p3 = PathParamRule.of("p3", INT)
    private val notRegistered = PathParamRule.of("none", STRING)

    private val params = PathParams(mapOf(p1.name to "aString", p2.name to 4))

    @Test
    fun get() {
        assertThat(params[p1], equalTo("aString"))
        assertThat(params[p2], equalTo(4))
        expect { params[p3] }.toThrow<IllegalArgumentException>()
        expect { params[notRegistered] }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun contains() {
        assertThat(p1 in params, equalTo(true))
        assertThat(p2 in params, equalTo(true))
        assertThat(p3 in params, equalTo(false))
        assertThat(notRegistered in params, equalTo(false))
    }
}
