/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class JsonBodyRequestTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val request = BlankJsonBodyRequest()

    @Test
    fun extract() {
        val jsonObject = json {
            obj(
                "string" to "value",
                "int" to 1,
                "double" to 2.2,
            )
        }

        assertThat(
            request.extract(jsonObject, "string") { obj, key -> obj.getString(key) },
            equalTo("value"),
        )
        assertThat(
            request.extract(jsonObject, "int") { obj, key -> obj.getInteger(key) },
            equalTo(1),
        )
        assertThat(
            request.extract(jsonObject, "double") { obj, key -> obj.getDouble(key) },
            equalTo(2.2),
        )

        expect { request.extract<Int>(jsonObject, "string") { obj, key -> obj.getInteger(key) } }
            .toThrow<IllegalArgumentException>()
            .withMessage("Error reading required key 'string'")

        expect { request.extract<Any>(jsonObject, "fake") { obj, key -> obj.getValue(key) } }
            .toThrow<IllegalArgumentException>()
            .withMessage("Required key 'fake' must be specified")
    }

    @Test
    fun extractList() {
        val jsonObject = json {
            obj(
                "objArray" to array(
                    obj(
                        "id" to 12,
                        "value" to "test1",
                    ),
                    obj(
                        "id" to 34,
                        "value" to "test2",
                    ),
                ),
                "emptyArray" to array(),
                "double" to 2.2,
            )
        }

        assertThat(
            request.extractList(jsonObject, "objArray", 2) { fromJson(it) },
            contains(TestDataPair(12, "test1"), TestDataPair(34, "test2")),
        )

        expect { request.extractList(jsonObject, "objArray", 3) { fromJson(it) } }
            .toThrow<IllegalArgumentException>()
            .withMessage("Required key 'objArray' must have at least 3 values")

        expect { request.extractList(jsonObject, "emptyArray", 1) { fromJson(it) } }
            .toThrow<IllegalArgumentException>()
            .withMessage("Required key 'emptyArray' must have at least 1 value")

        expect { request.extractList(jsonObject, "double", 3) { fromJson(it) } }
            .toThrow<IllegalArgumentException>()
            .withMessage("Error reading required key 'double'")

        expect { request.extractList(jsonObject, "fake", 3) { fromJson(it) } }
            .toThrow<IllegalArgumentException>()
            .withMessage("Required key 'fake' must be specified")
    }

    private fun fromJson(jsonObject: JsonObject): TestDataPair {
        return TestDataPair(jsonObject.getInteger("id"), jsonObject.getString("value"))
    }

    private class BlankJsonBodyRequest : JsonBodyRequest

    private data class TestDataPair(val id: Int, val value: String)

}
