/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.json.filter.JsonObjectFilter.Companion.applyFilter
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class JsonObjectFilterTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun filtersObject() {
        var jsonObject = filteredObject("key1,key3(key31,key32)")

        validateTopLevelKeys(jsonObject, expect1 = true, expect2 = false, expect3 = true, expect4 = false)
        val key3 = jsonObject.getJsonObject("key3")

        assertThat(key3.containsKey("key31"), equalTo(true))
        assertThat(key3.containsKey("key32"), equalTo(true))
        assertThat(key3.containsKey("key33"), equalTo(false))

        jsonObject = filteredObject("key2,key4(key41,key43(key431,key433))")

        validateTopLevelKeys(jsonObject, expect1 = false, expect2 = true, expect3 = false, expect4 = true)
        validateKey4(jsonObject, expect41 = true, expect433 = true, expect44 = false)

        jsonObject = filteredObject("key4(key43.key431,key44.key442)")

        validateTopLevelKeys(jsonObject, expect1 = false, expect2 = false, expect3 = false, expect4 = true)
        validateKey4(jsonObject, expect41 = false, expect433 = false, expect44 = true)

        jsonObject = filteredObject("key1.value")
        validateTopLevelKeys(jsonObject, expect1 = true, expect2 = false, expect3 = false, expect4 = false)

        jsonObject = filteredObject("-key1.value")
        validateTopLevelKeys(jsonObject, expect1 = true, expect2 = true, expect3 = true, expect4 = true)
    }

    private fun validateKey4(jsonObject: JsonObject, expect41: Boolean, expect433: Boolean, expect44: Boolean) {
        val key4 = jsonObject.getJsonArray("key4")

        assertThat(key4.size(), equalTo(2))
        validateArrayObjects(key4.getJsonArray(0), 3, 1, expect41, expect433, expect44)
        validateArrayObjects(key4.getJsonArray(1), 1, 4, expect41, expect433, expect44)
    }

    @Test
    fun filtersDoNotRemoveEmptyArrays() {
        val jsonObject = createObject()
        jsonObject.getJsonArray("key4").clear()

        applyFilter(jsonObject, FilterSpecification("key4.key43.key431"))

        validateTopLevelKeys(jsonObject, expect1 = false, expect2 = false, expect3 = false, expect4 = true)
        val key4 = jsonObject.getJsonArray("key4")

        assertThat(key4.size(), equalTo(0))
    }

    @Test
    fun isFluent() {
        val jsonObject = JsonObject()
        assertThat(applyFilter(jsonObject, FilterSpecification("test")), equalTo(jsonObject))
    }

    private fun validateTopLevelKeys(jsonObject: JsonObject, expect1: Boolean, expect2: Boolean, expect3: Boolean, expect4: Boolean) {
        assertThat(jsonObject.containsKey("key1"), equalTo(expect1))
        assertThat(jsonObject.containsKey("key2"), equalTo(expect2))
        assertThat(jsonObject.containsKey("key3"), equalTo(expect3))
        assertThat(jsonObject.containsKey("key4"), equalTo(expect4))
    }

    private fun validateArrayObjects(jsonArray: JsonArray, expectedSize: Int, startCount: Int, expect41: Boolean, expect433: Boolean, expect44: Boolean) {
        assertThat(jsonArray.size(), equalTo(expectedSize))

        (0..<expectedSize).forEach { i ->
            validateArrayObject(jsonArray.getJsonObject(i), (startCount + i).toDouble(), expect41, expect433, expect44)
        }
    }

    private fun validateArrayObject(jsonObject: JsonObject, count: Double, expect41: Boolean, expect433: Boolean, expect44: Boolean) {
        assertThat(jsonObject.containsKey("key41"), equalTo(expect41))
        assertThat(jsonObject.containsKey("key42"), equalTo(false))
        assertThat(jsonObject.containsKey("key43"), equalTo(true))
        assertThat(jsonObject.containsKey("key44"), equalTo(expect44))

        if (expect41)
            assertThat(jsonObject.getDouble("key41"), equalTo(41 + (count / 10)))

        val key43 = jsonObject.getJsonObject("key43")

        assertThat(key43.containsKey("key431"), equalTo(true))
        assertThat(key43.containsKey("key432"), equalTo(false))
        assertThat(key43.containsKey("key433"), equalTo(expect433))

        assertThat(key43.getDouble("key431"), equalTo(431 + (count / 10)))
        if (expect433)
            assertThat(key43.getDouble("key433"), equalTo(433 + (count / 10)))

        if (expect44) {
            val key44 = jsonObject.getJsonObject("key44")
            assertThat(key44.containsKey("key441"), equalTo(false))
            assertThat(key44.containsKey("key442"), equalTo(true))
            assertThat(key44.containsKey("key443"), equalTo(false))

            assertThat(key44.getDouble("key442"), equalTo(442 + (count / 10)))
        }
    }

    private fun filteredObject(filter: String): JsonObject =
        createObject().also {
            applyFilter(it, FilterSpecification(filter))
        }

    private fun createObject(): JsonObject {
        return json {
            obj(
                "key1" to 1,
                "key2" to "2",
                "key3" to createObject(3, 1.0),
                "key4" to array(
                    array(
                        createObject(4, 1.0),
                        createObject(4, 2.0),
                        createObject(4, 3.0),
                    ),
                    array(
                        createObject(4, 4.0),
                    ),
                ),
            )
        }
    }

    private fun createObject(key: Int, count: Double): JsonObject {
        return json {
            obj(
                "key" + key + "1" to (key * 10) + 1 + (count / 10),
                "key" + key + "2" to (key * 10) + 2 + (count / 10),
                "key" + key + "3" to obj(
                    "key" + key + "31" to (key * 100) + 31 + (count / 10),
                    "key" + key + "32" to (key * 100) + 32 + (count / 10),
                    "key" + key + "33" to (key * 100) + 33 + (count / 10),
                ),
                "key" + key + "4" to obj(
                    "key" + key + "41" to (key * 100) + 41 + (count / 10),
                    "key" + key + "42" to (key * 100) + 42 + (count / 10),
                    "key" + key + "43" to (key * 100) + 43 + (count / 10),
                ),
            )
        }
    }

}
