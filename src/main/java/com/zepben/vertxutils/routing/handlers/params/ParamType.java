/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.zepben.annotations.EverythingIsNonnullByDefault;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class ParamType {

    public static final RequestValueConverter<String, String> STRING = RequestValueConverter.create(
        "string",
        param -> {
            try {
                return URLDecoder.decode(param, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    public static final RequestValueConverter<String, Integer> INT = RequestValueConverter.create(
        "int",
        param -> {
            try {
                return Integer.parseInt(param);
            } catch (NumberFormatException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    public static final RequestValueConverter<String, Integer> INT_POSITIVE = RequestValueConverter.create(
        "positive int",
        param -> {
            try {
                return Integer.parseUnsignedInt(param);
            } catch (NumberFormatException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    public static final RequestValueConverter<String, Long> LONG = RequestValueConverter.create(
        "long",
        param -> {
            try {
                return Long.parseLong(param);
            } catch (NumberFormatException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    public static final RequestValueConverter<String, Long> LONG_POSITIVE = RequestValueConverter.create(
        "positive long",
        param -> {
            try {
                return Long.parseUnsignedLong(param);
            } catch (NumberFormatException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    public static final RequestValueConverter<String, Float> FLOAT = RequestValueConverter.create(
        "float",
        param -> {
            try {
                return Float.parseFloat(param);
            } catch (NumberFormatException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    @SuppressWarnings("RedundantTypeArguments")
    public static final RequestValueConverter<String, Float> FLOAT_POSITIVE = RequestValueConverter.create(
        "positive float",
        param -> {
            try {
                return Optional.of(Float.parseFloat(param))
                    .filter(f -> f >= 0)
                    .<ValueConversionException>orElseThrow(() -> new ValueConversionException("negative value"));
            } catch (NumberFormatException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    public static final RequestValueConverter<String, Double> DOUBLE = RequestValueConverter.create(
        "double",
        param -> {
            try {
                return Double.parseDouble(param);
            } catch (NumberFormatException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    @SuppressWarnings("RedundantTypeArguments")
    public static final RequestValueConverter<String, Double> DOUBLE_POSITIVE = RequestValueConverter.create(
        "positive double",
        param -> {
            try {
                return Optional.of(Double.parseDouble(param))
                    .filter(f -> f >= 0)
                    .<ValueConversionException>orElseThrow(() -> new ValueConversionException("negative value"));
            } catch (NumberFormatException e) {
                throw new ValueConversionException(e.getMessage());
            }
        });

    public static final RequestValueConverter<String, Boolean> BOOL = RequestValueConverter.create(
        "bool",
        param -> Boolean.parseBoolean(param) || param.equals("1"));

    public static final RequestValueConverter<String, LocalDate> LOCAL_DATE = RequestValueConverter.create(
        "ISO standard date (e.g. yyyy-mm-dd)",
        param -> {
            try {
                return LocalDate.parse(param);
            } catch (DateTimeParseException ex) {
                throw new ValueConversionException(ex.getMessage());
            }
        });

    public static final RequestValueConverter<String, LocalTime> LOCAL_TIME = RequestValueConverter.create(
        "ISO standard time (e.g. hh:mm)",
        param -> {
            // 0 pad the hours
            if (param.indexOf(":") == 1)
                param = "0" + param;

            try {
                return LocalTime.parse(param);
            } catch (DateTimeParseException ex) {
                throw new ValueConversionException(ex.getMessage());
            }
        });

    public static final RequestValueConverter<String, Instant> INSTANT = RequestValueConverter.create(
        "ISO standard UTC date time (e.g. YYYY:MM:DDTHH:mm:ss.sssZ)",
        param -> {
            try {
                return Instant.parse(param);
            } catch (DateTimeParseException ex) {
                throw new ValueConversionException(ex.getMessage());
            }
        });

    public static <T extends Enum<T>> RequestValueConverter<String, T> ofEnum(Class<T> clazz) {
        T[] enumConstants = clazz.getEnumConstants();

        return RequestValueConverter.create(
            Stream.of(enumConstants).map(Enum::name).collect(Collectors.joining(", ")),
            param -> {
                for (T value : enumConstants) {
                    if (value.name().equalsIgnoreCase(param))
                        return value;
                }

                throw new ValueConversionException("unsupported enum value");
            });
    }

}
