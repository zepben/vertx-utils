/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ParamTypeTest {

    @Test
    public void stringParam() {
        testValid(ParamType.STRING, "a%20string", "a string");
    }

    @Test
    public void invlidStringParam() {
        testInvalid(ParamType.STRING, "%x");
    }

    @Test
    public void intParam() {
        testValid(ParamType.INT, "-4", -4);
    }

    @Test
    public void invalidIntParam() {
        testInvalid(ParamType.INT, "bad");
    }

    @Test
    public void intPositiveParam() {
        testValid(ParamType.INT_POSITIVE, "4", 4);
    }

    @Test
    public void invalidIntPositiveParam() {
        testInvalid(ParamType.INT_POSITIVE, "-4");
    }

    @Test
    public void longParam() {
        testValid(ParamType.LONG, Long.toString(Long.MIN_VALUE), Long.MIN_VALUE);
    }

    @Test
    public void invalidLongParam() {
        testInvalid(ParamType.LONG, "bad");
    }

    @Test
    public void longPositiveParam() {
        testValid(ParamType.LONG_POSITIVE, Long.toString(Long.MAX_VALUE), Long.MAX_VALUE);
    }

    @Test
    public void invalidLongPositiveParam() {
        testInvalid(ParamType.LONG_POSITIVE, "-4");
    }

    @Test
    public void floatParam() {
        testValid(ParamType.FLOAT, "-1.2", -1.2f);
    }

    @Test
    public void invalidFloatParam() {
        testInvalid(ParamType.FLOAT, "bad");
    }

    @Test
    public void floatPositiveParam() {
        testValid(ParamType.FLOAT, "2.3", 2.3f);
    }

    @Test
    public void invalidFloatPositiveParam() {
        testInvalid(ParamType.FLOAT_POSITIVE, "-1.1");
    }

    @Test
    public void badFloatPositiveParam() {
        testInvalid(ParamType.FLOAT_POSITIVE, "bad");
    }

    @Test
    public void doubleParam() {
        testValid(ParamType.DOUBLE, "-1.2", -1.2);
    }

    @Test
    public void invalidDoubleParam() {
        testInvalid(ParamType.DOUBLE, "bad");
    }

    @Test
    public void doublePositiveParam() {
        testValid(ParamType.DOUBLE, "2.3", 2.3);
    }

    @Test
    public void badDoublePositiveParam() {
        testInvalid(ParamType.DOUBLE, "bad");
    }


    @Test
    public void invalidDoublePositiveParam() {
        testInvalid(ParamType.DOUBLE_POSITIVE, "-1.1");
    }

    @Test
    public void boolParam() {
        testValid(ParamType.BOOL, "tRuE", true);
        testValid(ParamType.BOOL, "1", true);
        testValid(ParamType.BOOL, "0", false);
        testValid(ParamType.BOOL, "fAlSe", false);
        testValid(ParamType.BOOL, "2", false);
        testValid(ParamType.BOOL, "yes", false);
    }

    @Test
    public void dateParam() {
        testValid(ParamType.LOCAL_DATE, "2018-06-25", LocalDate.of(2018, 6, 25));
        testInvalid(ParamType.LOCAL_DATE, "25-06-2018");
    }

    @Test
    public void timeParam() {
        testValid(ParamType.LOCAL_TIME, "6:45", LocalTime.of(6, 45));
        testValid(ParamType.LOCAL_TIME, "06:45", LocalTime.of(6, 45));
        testValid(ParamType.LOCAL_TIME, "20:18", LocalTime.of(20, 18));
        testInvalid(ParamType.LOCAL_TIME, "25:06");
    }

    @Test
    public void instantParam() {
        Instant now = Instant.now();
        testValid(ParamType.INSTANT, now.toString(), now);
        testInvalid(ParamType.INSTANT, "2018-06-01 12:01:14.000Z");
    }

    enum TestEnum {VALUE_1, VALUE_2}

    @Test
    public void enumParam() {
        testValid(ParamType.ofEnum(TestEnum.class), "VALUE_1", TestEnum.VALUE_1);
        testValid(ParamType.ofEnum(TestEnum.class), "VALUE_2", TestEnum.VALUE_2);
        testValid(ParamType.ofEnum(TestEnum.class), "value_1", TestEnum.VALUE_1);
        testValid(ParamType.ofEnum(TestEnum.class), "value_2", TestEnum.VALUE_2);
        testInvalid(ParamType.ofEnum(TestEnum.class), "VALUE_3");
    }

    private <T> void testValid(RequestValueConverter<String, T> converter, String param, T expected) {
        T value = converter.convert(param);
        assertThat(value, equalTo(expected));
    }

    private <T> void testInvalid(RequestValueConverter<String, T> converter, String param) {
        expect(() -> converter.convert(param)).toThrow(ValueConversionException.class);
    }
}
