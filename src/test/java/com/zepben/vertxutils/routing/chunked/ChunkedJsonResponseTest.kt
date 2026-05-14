/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.chunked

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * All tests in this suite call end on the response in order to capture the internal buffer for checking.
 */
class ChunkedJsonResponseTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val response: ChunkedJsonResponse = CaptureChunkedJsonResponse()

    @Test
    fun beginResponseObject() {
        response.ofObject {}
        assertThat(response.toString(), equalTo("{}"))

        // Shouldn't be able to start a new object while the buffer is not empty.
        expect { response.ofObject { } }
            .toThrow<IllegalStateException>()
            .withMessage("Can't reuse a non-clean response builder")

        // Shouldn't be able to start a new array while the buffer is not empty.
        expect { response.ofArray { } }
            .toThrow<IllegalStateException>()
            .withMessage("Can't reuse a non-clean response builder")
    }

    @Test
    fun beginResponseArray() {
        response.ofArray { }
        assertThat(response.toString(), equalTo("[]"))

        // Shouldn't be able to start a new object while the buffer is not empty.
        expect { response.ofObject { } }
            .toThrow<IllegalStateException>()
            .withMessage("Can't reuse a non-clean response builder")

        // Shouldn't be able to start a new array while the buffer is not empty.
        expect { response.ofArray { } }
            .toThrow<IllegalStateException>()
            .withMessage("Can't reuse a non-clean response builder")
    }

    @Test
    fun canReuseIfBufferIsCleared() {
        val reusable = ReuseableChunkedJsonResponse()

        reusable.ofObject { }
        reusable.ofObject { }
        reusable.ofArray { }
        reusable.ofArray { }
    }

    @Test
    fun generatesExpectedJson() {
        response.ofObject {
            obj("o1") {
                field("o1k1", 0)
                field("o1k2", "text")
                array("o1k3") {
                    array {
                        obj { }
                    }
                }
                array("o1k4") { }
            }
            obj("o2") {
                obj("o3") { }
                array("o2k1") {
                    item(0)
                    item(1)
                    item(2)
                }
            }
        }

        val expected = "{\"o1\":{\"o1k1\":0,\"o1k2\":\"text\",\"o1k3\":[[{}]],\"o1k4\":[]},\"o2\":{\"o3\":{},\"o2k1\":[0,1,2]}}"

        assertThat(response.toString(), equalTo(expected))
    }

    @Test
    fun `encodes JsonObject and JsonArray values`() {
        response.ofArray {
            item(jsonObjectOf("a" to 1, "b" to "c"))
            item(jsonArrayOf("d", 2, 3))
        }

        val expected = "[{\"a\":1,\"b\":\"c\"},[\"d\",2,3]]"

        assertThat(response.toString(), equalTo(expected))
    }

    private class ReuseableChunkedJsonResponse : ChunkedJsonResponse() {

        override fun checkWrite(sb: StringBuilder, force: Boolean) {
        }

        override fun onResponseCompleted(sb: StringBuilder) {
            sb.setLength(0)
        }
    }

}
