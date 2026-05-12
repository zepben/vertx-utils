/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

object ParamType {

    val STRING: RequestValueConverter<String, String> =
        RequestValueConverter.create("string") { param ->
            try {
                URLDecoder.decode(param, StandardCharsets.UTF_8.name())
            } catch (e: UnsupportedEncodingException) {
                throw ValueConversionException(e.message)
            } catch (e: IllegalArgumentException) {
                throw ValueConversionException(e.message)
            }
        }

    val INT: RequestValueConverter<String, Int> =
        RequestValueConverter.create("int") { param ->
            try {
                param.toInt()
            } catch (e: NumberFormatException) {
                throw ValueConversionException(e.message)
            }
        }

    val INT_POSITIVE: RequestValueConverter<String, Int> =
        RequestValueConverter.create("positive int") { param ->
            try {
                Integer.parseUnsignedInt(param)
            } catch (e: NumberFormatException) {
                throw ValueConversionException(e.message)
            }
        }

    val LONG: RequestValueConverter<String, Long> =
        RequestValueConverter.create("long") { param ->
            try {
                param.toLong()
            } catch (e: NumberFormatException) {
                throw ValueConversionException(e.message)
            }
        }

    val LONG_POSITIVE: RequestValueConverter<String, Long> =
        RequestValueConverter.create("positive long") { param ->
            try {
                java.lang.Long.parseUnsignedLong(param)
            } catch (e: NumberFormatException) {
                throw ValueConversionException(e.message)
            }
        }

    val FLOAT: RequestValueConverter<String, Float> =
        RequestValueConverter.create("float") { param ->
            try {
                param.toFloat()
            } catch (e: NumberFormatException) {
                throw ValueConversionException(e.message)
            }
        }

    val FLOAT_POSITIVE: RequestValueConverter<String, Float> =
        RequestValueConverter.create("positive float") { param ->
            try {
                param.toFloat().takeIf { it >= 0 } ?: throw ValueConversionException("negative value")
            } catch (e: NumberFormatException) {
                throw ValueConversionException(e.message)
            }
        }

    val DOUBLE: RequestValueConverter<String, Double> =
        RequestValueConverter.create("double") { param ->
            try {
                param.toDouble()
            } catch (e: NumberFormatException) {
                throw ValueConversionException(e.message)
            }
        }

    val DOUBLE_POSITIVE: RequestValueConverter<String, Double> =
        RequestValueConverter.create("positive double") { param ->
            try {
                param.toDouble().takeIf { it >= 0 } ?: throw ValueConversionException("negative value")
            } catch (e: NumberFormatException) {
                throw ValueConversionException(e.message)
            }
        }

    val BOOL: RequestValueConverter<String, Boolean> =
        RequestValueConverter.create("bool") { param -> param.toBoolean() || (param == "1") }

    val LOCAL_DATE: RequestValueConverter<String, LocalDate> =
        RequestValueConverter.create("ISO standard date (e.g. yyyy-mm-dd)") { param ->
            try {
                LocalDate.parse(param)
            } catch (ex: DateTimeParseException) {
                throw ValueConversionException(ex.message)
            }
        }

    val LOCAL_TIME: RequestValueConverter<String, LocalTime> =
        RequestValueConverter.create("ISO standard time (e.g. hh:mm)") { param ->
            try {
                // 0 pad the hours
                LocalTime.parse(if (param.indexOf(":") == 1) "0$param" else param)
            } catch (ex: DateTimeParseException) {
                throw ValueConversionException(ex.message)
            }
        }

    val INSTANT: RequestValueConverter<String, Instant> =
        RequestValueConverter.create("ISO standard UTC date time (e.g. YYYY:MM:DDTHH:mm:ss.sssZ)") { param ->
            try {
                Instant.parse(param)
            } catch (ex: DateTimeParseException) {
                throw ValueConversionException(ex.message)
            }
        }

    fun <T : Enum<T>> ofEnum(clazz: Class<T>): RequestValueConverter<String, T> {
        val enumConstants = clazz.getEnumConstants()

        return RequestValueConverter.create(enumConstants.joinToString { it.name }) { param ->
            enumConstants.firstOrNull { it.name.equals(param, ignoreCase = true) }
                ?: throw ValueConversionException("unsupported enum value")
        }
    }

}
