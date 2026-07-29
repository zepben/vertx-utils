/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.ErrorFormatter.asJson
import io.vertx.core.json.JsonObject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ErrorFormatterTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun errorToJson() {
        val err = "err"
        val actual = asJson(err)
        val expected = JsonObject().put("errors", listOf(err)).encode()
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun errorsToJson() {
        val errs = listOf("err1", "err2")
        val actual = asJson(errs)
        val expected = JsonObject().put("errors", errs).encode()
        assertThat(actual, equalTo(expected))
    }

}
