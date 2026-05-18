/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.nio.file.Path

typealias JsonArrayValueExtractor<T> = (jsonArray: JsonArray, pos: Int) -> T?

object JsonUtils {

    /**
     * Get the value from the specified key.
     * 
     * @receiver json object to extract the value from
     * @param key  the key containing the value
     * @return optional of the value
     */
    fun JsonObject.extractOptionalValue(key: String): Any? =
        getValue(key)

    /**
     * Get the value from the specified key.
     * 
     * @receiver json object to extract the value from
     * @param key  the key containing the value
     * @return the value
     * @throws ParsingException if the key is not found
     */
    fun JsonObject.extractRequiredValue(key: String): Any =
        this.extractRequired(key, "value") { obj, key -> obj.getValue(key) }

    /**
     * Get the object value from the specified key.
     * 
     * @receiver json object to extract the object from
     * @param key  the key containing the object
     * @return optional of the value
     * @throws ParsingException if the value is not an object
     */
    fun JsonObject.extractOptionalObject(key: String): JsonObject? =
        this.extractOptional(key, "object") { obj, key -> obj.getJsonObject(key) }

    /**
     * Get the object value from the specified key.
     * 
     * @receiver json object to extract the object from
     * @param key  the key containing the object
     * @return the value
     * @throws ParsingException if the value is not an object, or is not found
     */
    fun JsonObject.extractRequiredObject(key: String): JsonObject =
        this.extractRequired(key, "object") { obj, key -> obj.getJsonObject(key) }

    /**
     * Get the array value from the specified key.
     * 
     * @receiver json object to extract the array from
     * @param key  the key containing the array
     * @return optional of the value
     * @throws ParsingException if the value is not an array
     */
    fun JsonObject.extractOptionalArray(key: String): JsonArray? =
        this.extractOptional(key, "array") { obj, key -> obj.getJsonArray(key) }

    /**
     * Get the array value from the specified key.
     * 
     * @receiver json object to extract the array from
     * @param key  the key containing the array
     * @return the value
     * @throws ParsingException if the value is not an array, or is not found
     */
    fun JsonObject.extractRequiredArray(key: String): JsonArray =
        this.extractRequired(key, "array") { obj, key -> obj.getJsonArray(key) }

    /**
     * Get the string value from the specified key.
     * 
     * @receiver json object to extract the string from
     * @param key  the key containing the string
     * @return optional of the value
     * @throws ParsingException if the value is not a string
     */
    fun JsonObject.extractOptionalString(key: String): String? =
        this.extractOptional(key, "string") { obj, key -> JsonValueExtractors.getStringStrict(obj, key) }

    /**
     * Get the string value from the specified key.
     * 
     * @receiver json object to extract the string from
     * @param key  the key containing the string
     * @return the value
     * @throws ParsingException if the value is not a string, or is not found
     */
    fun JsonObject.extractRequiredString(key: String): String =
        this.extractRequired(key, "string") { obj, key -> JsonValueExtractors.getStringStrict(obj, key) }

    /**
     * Get the integer value from the specified key.
     * 
     * @receiver json object to extract the integer from
     * @param key  the key containing the integer
     * @return optional of the value
     * @throws ParsingException if the value is not an integer
     */
    fun JsonObject.extractOptionalInt(key: String): Int? =
        this.extractOptional(key, "integer") { obj, key -> obj.getInteger(key) }

    /**
     * Get the integer value from the specified key.
     * 
     * @receiver json object to extract the integer from
     * @param key  the key containing the integer
     * @return the value
     * @throws ParsingException if the value is not an integer, or is not found
     */
    fun JsonObject.extractRequiredInt(key: String): Int =
        this.extractRequired(key, "integer") { obj, key -> obj.getInteger(key) }

    /**
     * Get the double value from the specified key.
     * 
     * @receiver json object to extract the double from
     * @param key  the key containing the double
     * @return optional of the value
     * @throws ParsingException if the value is not a double
     */
    fun JsonObject.extractOptionalDouble(key: String): Double? =
        this.extractOptional(key, "double") { obj, key -> JsonValueExtractors.getDouble(obj, key) }

    /**
     * Get the double value from the specified key.
     * 
     * @receiver json object to extract the double from
     * @param key  the key containing the double
     * @return the value
     * @throws ParsingException if the value is not a double, or is not found
     */
    fun JsonObject.extractRequiredDouble(key: String): Double =
        this.extractRequired(key, "double") { obj, key -> JsonValueExtractors.getDouble(obj, key) }

    /**
     * Get the path value from the specified key.
     * 
     * @receiver json object to extract the path from
     * @param key  the key containing the path
     * @return optional of the value
     * @throws ParsingException if the value is not a path
     */
    fun JsonObject.extractOptionalPath(key: String): Path? =
        this.extractOptional(key, "path") { obj, key -> JsonValueExtractors.getPath(obj, key) }

    /**
     * Get the path value from the specified key.
     * 
     * @receiver json object to extract the path from
     * @param key  the key containing the path
     * @return the value
     * @throws ParsingException if the value is not a path, or is not found
     */
    fun JsonObject.extractRequiredPath(key: String): Path =
        this.extractRequired(key, "path") { obj, key -> JsonValueExtractors.getPath(obj, key) }

    /**
     * Get the JsonObjects from a JsonArray with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the JsonObjects contained in the specified array.
     * @throws ParsingException if the value is not a list of objects.
     */
    fun JsonObject.extractOptionalObjectList(key: String): List<JsonObject?>? =
        this.extractOptionalList(key, "objects") { jsonArray, pos -> jsonArray.getJsonObject(pos) }

    /**
     * Get the JsonObjects from a JsonArray with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the JsonObjects contained in the specified array.
     * @throws ParsingException if the value is not a list of objects, or is not found.
     */
    fun JsonObject.extractRequiredObjectList(key: String): List<JsonObject?> =
        this.extractRequiredList(key, "objects") { jsonArray, pos -> jsonArray.getJsonObject(pos) }

    /**
     * Get the strings from a JsonArray with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the strings contained in the specified array.
     * @throws ParsingException if the value is not a list of strings.
     */
    fun JsonObject.extractOptionalStringList(key: String): List<String?>? =
        this.extractOptionalList(key, "strings") { jsonArray, pos -> JsonValueExtractors.getStringStrict(jsonArray, pos) }

    /**
     * Get the strings from a JsonArray with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the strings contained in the specified array.
     * @throws ParsingException if the value is not a list of strings, or is not found.
     */
    fun JsonObject.extractRequiredStringList(key: String): List<String?> =
        this.extractRequiredList(key, "strings") { jsonArray, pos -> JsonValueExtractors.getStringStrict(jsonArray, pos) }

    /**
     * Get the integers from a JsonArray with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the integers contained in the specified array.
     * @throws ParsingException if the value is not a list of integers.
     */
    fun JsonObject.extractOptionalIntList(key: String): List<Int?>? =
        this.extractOptionalList(key, "integers") { jsonArray, pos -> jsonArray.getInteger(pos) }

    /**
     * Get the integers from a JsonArray with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the integers contained in the specified array.
     * @throws ParsingException if the value is not a list of integers, or is not found.
     */
    fun JsonObject.extractRequiredIntList(key: String): List<Int?> =
        this.extractRequiredList(key, "integers") { jsonArray, pos -> jsonArray.getInteger(pos) }

    /**
     * Get the doubles from a JsonArray with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the doubles contained in the specified array.
     * @throws ParsingException if the value is not a list of doubles.
     */
    fun JsonObject.extractOptionalDoubleList(key: String): List<Double?>? =
        this.extractOptionalList(key, "doubles") { jsonArray, pos -> JsonValueExtractors.getDouble(jsonArray, pos) }

    /**
     * Get the doubles from a JsonArray with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the doubles contained in the specified array.
     * @throws ParsingException if the value is not a list of doubles, or is not found.
     */
    fun JsonObject.extractRequiredDoubleList(key: String): List<Double?> =
        this.extractRequiredList(key, "doubles") { jsonArray, pos -> JsonValueExtractors.getDouble(jsonArray, pos) }

    /**
     * Get the JsonObjects from a JsonArray of JsonArrays with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list of lists containing each of the JsonObjects contained in the specified array or arrays.
     * @throws ParsingException if the value is not an object list.
     */
    fun JsonObject.extractOptionalObjectListOfList(key: String): List<List<JsonObject?>?>? =
        try {
            this.extractOptionalList(key, "object lists") { jsonArray, pos -> jsonArray.getJsonArray(pos) }
                ?.map { it?.doConvertToList { jsonArray, pos -> jsonArray.getJsonObject(pos) } }
        } catch (e: ClassCastException) {
            throw ParsingException("Value for '$key' is not a valid list of object lists.", e)
        }

    /**
     * Get the JsonObjects from a JsonArray of JsonArrays with the specified key as a list.
     * 
     * @receiver the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list of lists containing each of the JsonObjects contained in the specified array or arrays.
     * @throws ParsingException if the value is not an object list.
     */
    fun JsonObject.extractRequiredObjectListOfList(key: String): List<List<JsonObject?>?> =
        ensureRequired(key, extractOptionalObjectListOfList(key))

    /**
     * @receiver the array containing the objects.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    fun JsonArray.convertToObjectList(): List<JsonObject?> =
        this.convertToList { jsonArray, pos -> jsonArray.getJsonObject(pos) }

    /**
     * @receiver the array containing the objects.
     * @param valueExtractor the method used to extract the value from the array.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    fun <T> JsonArray.convertToList(valueExtractor: JsonArrayValueExtractor<T>): List<T?> =
        try {
            doConvertToList(valueExtractor)
        } catch (e: ClassCastException) {
            throw ParsingException("JSON array is not a collection of expected types.", e)
        }

    /**
     * @receiver the array containing the objects.
     * @param valueExtractor the method used to extract the value from the array.
     * @param expectedCount the expected number of entries in the list.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    fun <T> JsonArray.convertToList(expectedCount: Int, valueExtractor: JsonArrayValueExtractor<T>): List<T?> =
        if (size() == expectedCount)
            convertToList(valueExtractor)
        else
            throw ParsingException("Invalid number of records in list. Expected exactly $expectedCount, found ${size()}.")

    private fun <T> JsonObject.extractOptional(key: String, description: String, valueExtractor: (JsonObject, String) -> T?): T? =
        try {
            valueExtractor(this, key)
        } catch (e: Exception) {
            throw ParsingException("Value for '$key' is not a valid $description.", e)
        }

    private fun <T> JsonObject.extractOptionalList(key: String, description: String, valueExtractor: JsonArrayValueExtractor<T>): List<T?>? =
        try {
            extractOptionalArray(key)?.doConvertToList(valueExtractor)
        } catch (e: ClassCastException) {
            throw ParsingException("Value for '$key' is not a valid list of $description.", e)
        }

    fun <T> JsonObject.extractRequired(key: String, description: String, valueExtractor: (JsonObject, String) -> T?): T =
        ensureRequired(key, extractOptional(key, description, valueExtractor))

    fun <T> JsonObject.extractRequiredList(key: String, description: String, valueExtractor: JsonArrayValueExtractor<T>): List<T?> =
        ensureRequired(key, extractOptionalList(key, description, valueExtractor))

    fun <T> ensureRequired(key: String, value: T?): T =
        value ?: throw ParsingException("No value found for required key '$key'.")

    private fun <T> JsonArray.doConvertToList(valueExtractor: JsonArrayValueExtractor<T>): List<T?> =
        (0..<size()).asSequence().map { pos -> valueExtractor(this, pos) }.toList()

    class ParsingException(message: String, cause: Throwable? = null) : Exception(message, cause)

}
