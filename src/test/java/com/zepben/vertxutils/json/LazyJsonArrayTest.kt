/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json

import com.zepben.testutils.junit.SystemLogExtension
import io.vertx.core.json.JsonArray
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class LazyJsonArrayTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun smokeTest() {
        val lja = LazyJsonArray {
            JsonArray(listOf(*(0..<100).toList().toTypedArray()))
        }

        assertThat(lja.size(), equalTo(100))
        assertThat(lja.getInteger(99), equalTo(99))
    }

}
