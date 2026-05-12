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
        testValid(ParamType.INSTANCE.getSTRING(), "a%20string", "a string");
    }

    @Test
    public void invalidStringParam() {
        testInvalid(ParamType.INSTANCE.getSTRING(), "%x");
    }

    @Test
    public void intParam() {
        testValid(ParamType.INSTANCE.getINT(), "-4", -4);
    }

    @Test
    public void invalidIntParam() {
        testInvalid(ParamType.INSTANCE.getINT(), "bad");
    }

    @Test
    public void intPositiveParam() {
        testValid(ParamType.INSTANCE.getINT_POSITIVE(), "4", 4);
    }

    @Test
    public void invalidIntPositiveParam() {
        testInvalid(ParamType.INSTANCE.getINT_POSITIVE(), "-4");
    }

    @Test
    public void longParam() {
        testValid(ParamType.INSTANCE.getLONG(), Long.toString(Long.MIN_VALUE), Long.MIN_VALUE);
    }

    @Test
    public void invalidLongParam() {
        testInvalid(ParamType.INSTANCE.getLONG(), "bad");
    }

    @Test
    public void longPositiveParam() {
        testValid(ParamType.INSTANCE.getLONG_POSITIVE(), Long.toString(Long.MAX_VALUE), Long.MAX_VALUE);
    }

    @Test
    public void invalidLongPositiveParam() {
        testInvalid(ParamType.INSTANCE.getLONG_POSITIVE(), "-4");
    }

    @Test
    public void floatParam() {
        testValid(ParamType.INSTANCE.getFLOAT(), "-1.2", -1.2f);
    }

    @Test
    public void invalidFloatParam() {
        testInvalid(ParamType.INSTANCE.getFLOAT(), "bad");
    }

    @Test
    public void floatPositiveParam() {
        testValid(ParamType.INSTANCE.getFLOAT(), "2.3", 2.3f);
    }

    @Test
    public void invalidFloatPositiveParam() {
        testInvalid(ParamType.INSTANCE.getFLOAT_POSITIVE(), "-1.1");
    }

    @Test
    public void badFloatPositiveParam() {
        testInvalid(ParamType.INSTANCE.getFLOAT_POSITIVE(), "bad");
    }

    @Test
    public void doubleParam() {
        testValid(ParamType.INSTANCE.getDOUBLE(), "-1.2", -1.2);
    }

    @Test
    public void invalidDoubleParam() {
        testInvalid(ParamType.INSTANCE.getDOUBLE(), "bad");
    }

    @Test
    public void doublePositiveParam() {
        testValid(ParamType.INSTANCE.getDOUBLE(), "2.3", 2.3);
    }

    @Test
    public void badDoublePositiveParam() {
        testInvalid(ParamType.INSTANCE.getDOUBLE(), "bad");
    }


    @Test
    public void invalidDoublePositiveParam() {
        testInvalid(ParamType.INSTANCE.getDOUBLE_POSITIVE(), "-1.1");
    }

    @Test
    public void boolParam() {
        testValid(ParamType.INSTANCE.getBOOL(), "tRuE", true);
        testValid(ParamType.INSTANCE.getBOOL(), "1", true);
        testValid(ParamType.INSTANCE.getBOOL(), "0", false);
        testValid(ParamType.INSTANCE.getBOOL(), "fAlSe", false);
        testValid(ParamType.INSTANCE.getBOOL(), "2", false);
        testValid(ParamType.INSTANCE.getBOOL(), "yes", false);
    }

    @Test
    public void dateParam() {
        testValid(ParamType.INSTANCE.getLOCAL_DATE(), "2018-06-25", LocalDate.of(2018, 6, 25));
        testInvalid(ParamType.INSTANCE.getLOCAL_DATE(), "25-06-2018");
    }

    @Test
    public void timeParam() {
        testValid(ParamType.INSTANCE.getLOCAL_TIME(), "6:45", LocalTime.of(6, 45));
        testValid(ParamType.INSTANCE.getLOCAL_TIME(), "06:45", LocalTime.of(6, 45));
        testValid(ParamType.INSTANCE.getLOCAL_TIME(), "20:18", LocalTime.of(20, 18));
        testInvalid(ParamType.INSTANCE.getLOCAL_TIME(), "25:06");
    }

    @Test
    public void instantParam() {
        Instant now = Instant.now();
        testValid(ParamType.INSTANCE.getINSTANT(), now.toString(), now);
        testInvalid(ParamType.INSTANCE.getINSTANT(), "2018-06-01 12:01:14.000Z");
    }

    enum TestEnum {VALUE_1, VALUE_2}

    @Test
    public void enumParam() {
        testValid(ParamType.INSTANCE.ofEnum(TestEnum.class), "VALUE_1", TestEnum.VALUE_1);
        testValid(ParamType.INSTANCE.ofEnum(TestEnum.class), "VALUE_2", TestEnum.VALUE_2);
        testValid(ParamType.INSTANCE.ofEnum(TestEnum.class), "value_1", TestEnum.VALUE_1);
        testValid(ParamType.INSTANCE.ofEnum(TestEnum.class), "value_2", TestEnum.VALUE_2);
        testInvalid(ParamType.INSTANCE.ofEnum(TestEnum.class), "VALUE_3");
    }

    private <T> void testValid(RequestValueConverter<String, T> converter, String param, T expected) {
        T value = converter.convert(param);
        assertThat(value, equalTo(expected));
    }

    private <T> void testInvalid(RequestValueConverter<String, T> converter, String param) {
        expect(() -> converter.convert(param)).toThrow(ValueConversionException.class);
    }
}
