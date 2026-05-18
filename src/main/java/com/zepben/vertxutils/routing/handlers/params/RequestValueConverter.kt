/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

interface RequestValueConverter<T, R> {

    /**
     * Converts the given value from a raw request type to its required type.
     * 
     * 
     * This could be a string from a parameter or a buffer from a body.
     * 
     * 
     * If the given value cannot be converted, this method should throw a [ValueConversionException].
     * If a null value is returned it will be treated as a generic "failed conversion" error and a message like
     * "conversion resulted in null value" will be supplied as the reason for failure.
     * 
     * @param param the value from the request
     * @return The converted value, or null in the case the conversion fails.
     * @throws ValueConversionException if a value cannot be converted.
     */
    fun convert(param: T): R

    /**
     * Returns a format description that the converter expects. This could be as simple as "string" or "int", but
     * may can be anything that describes how the value should be formatted. For example a date might be "YYYY-MM-DD".
     * 
     * @return A description of the expected format of values that can be converted.
     */
    val expectedFormat: String

    companion object {

        /**
         * Factory method to create an instance.
         * 
         * @param converter      Function to convert the param.
         * @param expectedFormat The expected format of the param.
         * @param <T>            The type of parameter to be converted.
         * @param <R>            The return type of the conversion.
         * @return a new RequestValueConverter instance.
         */
        fun <T, R> create(expectedFormat: String, converter: (T) -> R): RequestValueConverter<T, R> =
            object : RequestValueConverter<T, R> {

                override fun convert(param: T): R = converter(param)
                override val expectedFormat: String = expectedFormat

            }

    }

}
