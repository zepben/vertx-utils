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
import com.zepben.vertxutils.routing.handlers.params.ParamType.BOOL
import com.zepben.vertxutils.routing.handlers.params.ParamType.DOUBLE
import com.zepben.vertxutils.routing.handlers.params.ParamType.DOUBLE_POSITIVE
import com.zepben.vertxutils.routing.handlers.params.ParamType.FLOAT
import com.zepben.vertxutils.routing.handlers.params.ParamType.FLOAT_POSITIVE
import com.zepben.vertxutils.routing.handlers.params.ParamType.INSTANT
import com.zepben.vertxutils.routing.handlers.params.ParamType.INT
import com.zepben.vertxutils.routing.handlers.params.ParamType.INT_POSITIVE
import com.zepben.vertxutils.routing.handlers.params.ParamType.LOCAL_DATE
import com.zepben.vertxutils.routing.handlers.params.ParamType.LOCAL_TIME
import com.zepben.vertxutils.routing.handlers.params.ParamType.LONG
import com.zepben.vertxutils.routing.handlers.params.ParamType.LONG_POSITIVE
import com.zepben.vertxutils.routing.handlers.params.ParamType.STRING
import com.zepben.vertxutils.routing.handlers.params.ParamType.ofEnum
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class ParamTypeTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun stringParam() {
        testValid(STRING, "a%20string", "a string")
    }

    @Test
    fun invalidStringParam() {
        testInvalid(STRING, "%x")
    }

    @Test
    fun intParam() {
        testValid(INT, "-4", -4)
    }

    @Test
    fun invalidIntParam() {
        testInvalid(INT, "bad")
    }

    @Test
    fun intPositiveParam() {
        testValid(INT_POSITIVE, "4", 4)
    }

    @Test
    fun invalidIntPositiveParam() {
        testInvalid(INT_POSITIVE, "-4")
    }

    @Test
    fun longParam() {
        testValid(LONG, Long.MIN_VALUE.toString(), Long.MIN_VALUE)
    }

    @Test
    fun invalidLongParam() {
        testInvalid(LONG, "bad")
    }

    @Test
    fun longPositiveParam() {
        testValid(LONG_POSITIVE, Long.MAX_VALUE.toString(), Long.MAX_VALUE)
    }

    @Test
    fun invalidLongPositiveParam() {
        testInvalid(LONG_POSITIVE, "-4")
    }

    @Test
    fun floatParam() {
        testValid(FLOAT, "-1.2", -1.2f)
    }

    @Test
    fun invalidFloatParam() {
        testInvalid(FLOAT, "bad")
    }

    @Test
    fun floatPositiveParam() {
        testValid(FLOAT, "2.3", 2.3f)
    }

    @Test
    fun invalidFloatPositiveParam() {
        testInvalid(FLOAT_POSITIVE, "-1.1")
    }

    @Test
    fun badFloatPositiveParam() {
        testInvalid(FLOAT_POSITIVE, "bad")
    }

    @Test
    fun doubleParam() {
        testValid(DOUBLE, "-1.2", -1.2)
    }

    @Test
    fun invalidDoubleParam() {
        testInvalid(DOUBLE, "bad")
    }

    @Test
    fun doublePositiveParam() {
        testValid(DOUBLE, "2.3", 2.3)
    }

    @Test
    fun badDoublePositiveParam() {
        testInvalid(DOUBLE, "bad")
    }

    @Test
    fun invalidDoublePositiveParam() {
        testInvalid(DOUBLE_POSITIVE, "-1.1")
    }

    @Test
    fun boolParam() {
        testValid(BOOL, "tRuE", true)
        testValid(BOOL, "1", true)
        testValid(BOOL, "0", false)
        testValid(BOOL, "fAlSe", false)
        testValid(BOOL, "2", false)
        testValid(BOOL, "yes", false)
    }

    @Test
    fun dateParam() {
        testValid(LOCAL_DATE, "2018-06-25", LocalDate.of(2018, 6, 25))
        testInvalid(LOCAL_DATE, "25-06-2018")
    }

    @Test
    fun timeParam() {
        testValid(LOCAL_TIME, "6:45", LocalTime.of(6, 45))
        testValid(LOCAL_TIME, "06:45", LocalTime.of(6, 45))
        testValid(LOCAL_TIME, "20:18", LocalTime.of(20, 18))
        testInvalid(LOCAL_TIME, "25:06")
    }

    @Test
    fun instantParam() {
        val now = Instant.now()
        testValid(INSTANT, now.toString(), now)
        testInvalid(INSTANT, "2018-06-01 12:01:14.000Z")
    }

    internal enum class TestEnum {
        VALUE_1, VALUE_2
    }

    @Test
    fun enumParam() {
        testValid(ofEnum(), "VALUE_1", TestEnum.VALUE_1)
        testValid(ofEnum(), "VALUE_2", TestEnum.VALUE_2)
        testValid(ofEnum(), "value_1", TestEnum.VALUE_1)
        testValid(ofEnum(), "value_2", TestEnum.VALUE_2)
        testInvalid(ofEnum<TestEnum>(), "VALUE_3")
    }

    private fun <T> testValid(converter: RequestValueConverter<String, T>, param: String, expected: T) {
        assertThat(converter.convert(param), equalTo(expected))
    }

    private fun <T> testInvalid(converter: RequestValueConverter<String, T>, param: String) {
        expect { converter.convert(param) }.toThrow<ValueConversionException>()
    }

}
