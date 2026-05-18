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
import com.zepben.vertxutils.routing.handlers.params.BodyType.JSON_ARRAY
import com.zepben.vertxutils.routing.handlers.params.BodyType.JSON_OBJECT
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.impl.RequestBodyImpl
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mockito.mock

class BodyTypeTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    var body: RequestBodyImpl = RequestBodyImpl(mock())

    @Test
    fun jsonObject() {
        val jsonObject = JsonObject().put("test", "value")
        body.setBuffer(Buffer.buffer(jsonObject.encode()))

        val converted = JSON_OBJECT.convert(body)
        assertThat(converted, equalTo(jsonObject))
    }

    @Test
    fun badJsonObjectReturnsNull() {
        body.setBuffer(Buffer.buffer("rubbish"))

        expect { JSON_OBJECT.convert(body) }.toThrow<ValueConversionException>()
    }

    @Test
    fun emptyJsonObjectReturnsNull() {
        body.setBuffer(Buffer.buffer())

        expect { JSON_OBJECT.convert(body) }.toThrow<ValueConversionException>()
    }

    @Test
    fun jsonArray() {
        val jsonArray = JsonArray().add(1).add("a string")
        body.setBuffer(Buffer.buffer(jsonArray.encode()))
        val converted = JSON_ARRAY.convert(body)
        assertThat(converted, equalTo(jsonArray))
    }

    @Test
    fun badJsonArrayReturnsNull() {
        body.setBuffer(Buffer.buffer("rubbish"))
        expect { JSON_ARRAY.convert(body) }.toThrow<ValueConversionException>()
    }

    @Test
    fun emptyJsonArrayReturnsNull() {
        body.setBuffer(Buffer.buffer())
        expect { JSON_ARRAY.convert(body) }.toThrow<ValueConversionException>()
    }

}
