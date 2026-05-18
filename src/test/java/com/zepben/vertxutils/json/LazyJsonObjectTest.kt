/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json

import com.zepben.testutils.junit.SystemLogExtension
import io.vertx.core.json.JsonObject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class LazyJsonObjectTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

        private const val KEY1 = "pi"
        private const val KEY2 = "zepben"
        private const val VALUE1 = Math.PI
        private const val VALUE2 = "Zeppelin Bend"
    }

    @Test
    fun lazyObjectTest() {
        val ljo = LazyJsonObject {
            val jsonObject = JsonObject()
            jsonObject.put(KEY1, VALUE1)
            jsonObject.put(KEY2, VALUE2)
            jsonObject
        }

        val encoded = ljo.encode()
        val decoded = JsonObject(encoded)

        assertThat(decoded.getDouble(KEY1), equalTo(VALUE1))
        assertThat(decoded.getString(KEY2), equalTo(VALUE2))
    }

    @Test
    fun lazyIncludedMemberTest() {
        var callCount = 0

        val jsonObject = LazyJsonObject()
        jsonObject.put(KEY1) {
            ++callCount
            VALUE1
        }
        jsonObject.put(KEY2, VALUE2)

        val encoded = jsonObject.encode()
        val decoded = JsonObject(encoded)

        assertThat(decoded.getDouble(KEY1), equalTo(VALUE1))
        assertThat(decoded.getString(KEY2), equalTo(VALUE2))
        assertThat(callCount, equalTo(1))
    }

    @Test
    fun lazyExcludedMemberTest() {
        var callCount = 0

        val jsonObject = LazyJsonObject()
        jsonObject.put(KEY1) {
            ++callCount
            VALUE1
        }
        jsonObject.put(KEY2, VALUE2)

        jsonObject.remove(KEY1)

        val encoded = jsonObject.encode()
        val decoded = JsonObject(encoded)

        assertThat(decoded.getDouble(KEY1), nullValue())
        assertThat(decoded.getString(KEY2), equalTo(VALUE2))
        assertThat(callCount, equalTo(0))
    }

    @Test
    fun lazyObjectWithIncludedLazyMemberTest() {
        var callCount = 0

        val ljo = LazyJsonObject {
            val jsonObject = LazyJsonObject()
            jsonObject.put(KEY1) {
                ++callCount
                VALUE1
            }
            jsonObject.put(KEY2, VALUE2)
            jsonObject
        }

        val encoded = ljo.encode()
        val decoded = JsonObject(encoded)

        assertThat(decoded.getDouble(KEY1), equalTo(VALUE1))
        assertThat(decoded.getString(KEY2), equalTo(VALUE2))

        assertThat(callCount, equalTo(1))
    }

    @Test
    fun lazyObjectWithExcludedLazyMemberTest() {
        var callCount = 0

        val ljo = LazyJsonObject {
            val jsonObject = LazyJsonObject()
            jsonObject.put(KEY1) {
                ++callCount
                VALUE1
            }
            jsonObject.put(KEY2, VALUE2)
            jsonObject
        }

        ljo.remove(KEY1)

        val encoded = ljo.encode()

        val decoded = JsonObject(encoded)

        assertThat(decoded.getDouble(KEY1), nullValue())
        assertThat(decoded.getString(KEY2), equalTo(VALUE2))

        assertThat(callCount, equalTo(0))
    }

}
