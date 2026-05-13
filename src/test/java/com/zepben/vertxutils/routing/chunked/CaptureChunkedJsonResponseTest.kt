/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.chunked

import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CaptureChunkedJsonResponseTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun captured() {
        val response = CaptureChunkedJsonResponse()

        response.ofArray {
            item("this is")
            assertThat(response.toString(), equalTo("[\"this is\""))

            item("my")
            assertThat(response.toString(), equalTo("[\"this is\",\"my\""))

            item("test data")
            assertThat(response.toString(), equalTo("[\"this is\",\"my\",\"test data\""))
        }

        assertThat(response.toString(), equalTo("[\"this is\",\"my\",\"test data\"]"))
    }

}
