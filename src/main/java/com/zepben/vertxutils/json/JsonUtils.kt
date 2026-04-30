/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
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
     * @param json json object to extract the value from
     * @param key  the key containing the value
     * @return optional of the value
     */
    fun extractOptionalValue(json: JsonObject, key: String): Any? =
        json.getValue(key)

    /**
     * Get the value from the specified key.
     * 
     * @param json json object to extract the value from
     * @param key  the key containing the value
     * @return the value
     * @throws ParsingException if the key is not found
     */
    @Throws(ParsingException::class)
    fun extractRequiredValue(json: JsonObject, key: String): Any =
        extractRequired(json, key, "value") { obj, key -> obj.getValue(key) }

    /**
     * Get the object value from the specified key.
     * 
     * @param json json object to extract the object from
     * @param key  the key containing the object
     * @return optional of the value
     * @throws ParsingException if the value is not an object
     */
    @Throws(ParsingException::class)
    fun extractOptionalObject(json: JsonObject, key: String): JsonObject? =
        extractOptional(json, key, "object") { obj, key -> obj.getJsonObject(key) }

    /**
     * Get the object value from the specified key.
     * 
     * @param json json object to extract the object from
     * @param key  the key containing the object
     * @return the value
     * @throws ParsingException if the value is not an object, or is not found
     */
    @Throws(ParsingException::class)
    fun extractRequiredObject(json: JsonObject, key: String): JsonObject =
        extractRequired(json, key, "object") { obj, key -> obj.getJsonObject(key) }

    /**
     * Get the array value from the specified key.
     * 
     * @param json json object to extract the array from
     * @param key  the key containing the array
     * @return optional of the value
     * @throws ParsingException if the value is not an array
     */
    @Throws(ParsingException::class)
    fun extractOptionalArray(json: JsonObject, key: String): JsonArray? =
        extractOptional(json, key, "array") { obj, key -> obj.getJsonArray(key) }

    /**
     * Get the array value from the specified key.
     * 
     * @param json json object to extract the array from
     * @param key  the key containing the array
     * @return the value
     * @throws ParsingException if the value is not an array, or is not found
     */
    @Throws(ParsingException::class)
    fun extractRequiredArray(json: JsonObject, key: String): JsonArray =
        extractRequired(json, key, "array") { obj, key -> obj.getJsonArray(key) }

    /**
     * Get the string value from the specified key.
     * 
     * @param json json object to extract the string from
     * @param key  the key containing the string
     * @return optional of the value
     * @throws ParsingException if the value is not a string
     */
    @Throws(ParsingException::class)
    fun extractOptionalString(json: JsonObject, key: String): String? =
        extractOptional(json, key, "string") { obj, key -> JsonValueExtractors.getStringStrict(obj, key) }

    /**
     * Get the string value from the specified key.
     * 
     * @param json json object to extract the string from
     * @param key  the key containing the string
     * @return the value
     * @throws ParsingException if the value is not a string, or is not found
     */
    @Throws(ParsingException::class)
    fun extractRequiredString(json: JsonObject, key: String): String =
        extractRequired(json, key, "string") { obj, key -> JsonValueExtractors.getStringStrict(obj, key) }

    /**
     * Get the integer value from the specified key.
     * 
     * @param json json object to extract the integer from
     * @param key  the key containing the integer
     * @return optional of the value
     * @throws ParsingException if the value is not an integer
     */
    @Throws(ParsingException::class)
    fun extractOptionalInt(json: JsonObject, key: String): Int? =
        extractOptional(json, key, "integer") { obj, key -> obj.getInteger(key) }

    /**
     * Get the integer value from the specified key.
     * 
     * @param json json object to extract the integer from
     * @param key  the key containing the integer
     * @return the value
     * @throws ParsingException if the value is not an integer, or is not found
     */
    @Throws(ParsingException::class)
    fun extractRequiredInt(json: JsonObject, key: String): Int =
        extractRequired(json, key, "integer") { obj, key -> obj.getInteger(key) }

    /**
     * Get the double value from the specified key.
     * 
     * @param json json object to extract the double from
     * @param key  the key containing the double
     * @return optional of the value
     * @throws ParsingException if the value is not a double
     */
    @Throws(ParsingException::class)
    fun extractOptionalDouble(json: JsonObject, key: String): Double? =
        extractOptional(json, key, "double") { obj, key -> JsonValueExtractors.getDouble(obj, key) }

    /**
     * Get the double value from the specified key.
     * 
     * @param json json object to extract the double from
     * @param key  the key containing the double
     * @return the value
     * @throws ParsingException if the value is not a double, or is not found
     */
    @Throws(ParsingException::class)
    fun extractRequiredDouble(json: JsonObject, key: String): Double =
        extractRequired(json, key, "double") { obj, key -> JsonValueExtractors.getDouble(obj, key) }

    /**
     * Get the path value from the specified key.
     * 
     * @param json json object to extract the path from
     * @param key  the key containing the path
     * @return optional of the value
     * @throws ParsingException if the value is not a path
     */
    @Throws(ParsingException::class)
    fun extractOptionalPath(json: JsonObject, key: String): Path? =
        extractOptional(json, key, "path") { obj, key -> JsonValueExtractors.getPath(obj, key) }

    /**
     * Get the path value from the specified key.
     * 
     * @param json json object to extract the path from
     * @param key  the key containing the path
     * @return the value
     * @throws ParsingException if the value is not a path, or is not found
     */
    @Throws(ParsingException::class)
    fun extractRequiredPath(json: JsonObject, key: String): Path =
        extractRequired(json, key, "path") { obj, key -> JsonValueExtractors.getPath(obj, key) }

    /**
     * Get the JsonObjects from a JsonArray with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the JsonObjects contained in the specified array.
     * @throws ParsingException if the value is not a list of objects.
     */
    @Throws(ParsingException::class)
    fun extractOptionalObjectList(json: JsonObject, key: String): List<JsonObject?>? =
        extractOptionalList(json, key, "objects") { jsonArray, pos -> jsonArray.getJsonObject(pos) }

    /**
     * Get the JsonObjects from a JsonArray with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the JsonObjects contained in the specified array.
     * @throws ParsingException if the value is not a list of objects, or is not found.
     */
    @Throws(ParsingException::class)
    fun extractRequiredObjectList(json: JsonObject, key: String): List<JsonObject?> =
        extractRequiredList(json, key, "objects") { jsonArray, pos -> jsonArray.getJsonObject(pos) }

    /**
     * Get the strings from a JsonArray with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the strings contained in the specified array.
     * @throws ParsingException if the value is not a list of strings.
     */
    @Throws(ParsingException::class)
    fun extractOptionalStringList(json: JsonObject, key: String): List<String?>? =
        extractOptionalList(json, key, "strings") { jsonArray, pos -> JsonValueExtractors.getStringStrict(jsonArray, pos) }

    /**
     * Get the strings from a JsonArray with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the strings contained in the specified array.
     * @throws ParsingException if the value is not a list of strings, or is not found.
     */
    @Throws(ParsingException::class)
    fun extractRequiredStringList(json: JsonObject, key: String): List<String?> =
        extractRequiredList(json, key, "strings") { jsonArray, pos -> JsonValueExtractors.getStringStrict(jsonArray, pos) }

    /**
     * Get the integers from a JsonArray with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the integers contained in the specified array.
     * @throws ParsingException if the value is not a list of integers.
     */
    @Throws(ParsingException::class)
    fun extractOptionalIntList(json: JsonObject, key: String): List<Int?>? =
        extractOptionalList(json, key, "integers") { jsonArray, pos -> jsonArray.getInteger(pos) }

    /**
     * Get the integers from a JsonArray with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the integers contained in the specified array.
     * @throws ParsingException if the value is not a list of integers, or is not found.
     */
    @Throws(ParsingException::class)
    fun extractRequiredIntList(json: JsonObject, key: String): List<Int?> =
        extractRequiredList(json, key, "integers") { jsonArray, pos -> jsonArray.getInteger(pos) }

    /**
     * Get the doubles from a JsonArray with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the doubles contained in the specified array.
     * @throws ParsingException if the value is not a list of doubles.
     */
    @Throws(ParsingException::class)
    fun extractOptionalDoubleList(json: JsonObject, key: String): List<Double?>? =
        extractOptionalList(json, key, "doubles") { jsonArray, pos -> JsonValueExtractors.getDouble(jsonArray, pos) }

    /**
     * Get the doubles from a JsonArray with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the doubles contained in the specified array.
     * @throws ParsingException if the value is not a list of doubles, or is not found.
     */
    @Throws(ParsingException::class)
    fun extractRequiredDoubleList(json: JsonObject, key: String): List<Double?> =
        extractRequiredList(json, key, "doubles") { jsonArray, pos -> JsonValueExtractors.getDouble(jsonArray, pos) }

    /**
     * Get the JsonObjects from a JsonArray of JsonArrays with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list of lists containing each of the JsonObjects contained in the specified array or arrays.
     * @throws ParsingException if the value is not an object list.
     */
    @Throws(ParsingException::class)
    fun extractOptionalObjectListOfList(json: JsonObject, key: String): List<List<JsonObject?>?>? =
        try {
            extractOptionalList(json, key, "object lists") { jsonArray, pos -> jsonArray.getJsonArray(pos) }
                ?.map { it?.convertToList { jsonArray, pos -> jsonArray.getJsonObject(pos) } }
        } catch (e: ClassCastException) {
            throw ParsingException(String.format("Value for '%s' is not a valid list of object lists.", key), e)
        }

    /**
     * Get the JsonObjects from a JsonArray of JsonArrays with the specified key as a list.
     * 
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list of lists containing each of the JsonObjects contained in the specified array or arrays.
     * @throws ParsingException if the value is not an object list.
     */
    @Throws(ParsingException::class)
    fun extractRequiredObjectListOfList(json: JsonObject, key: String): List<List<JsonObject?>?> =
        ensureRequired(key, extractOptionalObjectListOfList(json, key))

    /**
     * @param jsonArray the array containing the objects.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    @Throws(ParsingException::class)
    fun convertToObjectList(jsonArray: JsonArray): List<JsonObject?> =
        convertToList(jsonArray) { jsonArray, pos -> jsonArray.getJsonObject(pos) }

    /**
     * @param jsonArray the array containing the objects.
     * @param valueExtractor the method used to extract the value from the array.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    @Throws(ParsingException::class)
    fun <T> convertToList(jsonArray: JsonArray, valueExtractor: JsonArrayValueExtractor<T>): List<T?> =
        try {
            jsonArray.convertToList(valueExtractor)
        } catch (e: ClassCastException) {
            throw ParsingException("JSON array is not a collection of expected types.", e)
        }

    /**
     * @param jsonArray the array containing the objects.
     * @param valueExtractor the method used to extract the value from the array.
     * @param expectedCount the expected number of entries in the list.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    @Throws(ParsingException::class)
    fun <T> convertToList(jsonArray: JsonArray, valueExtractor: JsonArrayValueExtractor<T>, expectedCount: Int): List<T?> =
        if (jsonArray.size() == expectedCount)
            convertToList(jsonArray, valueExtractor)
        else
            throw ParsingException("Invalid number of records in list. Expected exactly $expectedCount, found ${jsonArray.size()}.")

    @Throws(ParsingException::class)
    private fun <T> extractOptional(json: JsonObject, key: String, description: String, valueExtractor: ValueExtractor<T>): T? =
        try {
            valueExtractor.extract(json, key)
        } catch (e: Exception) {
            throw ParsingException(String.format("Value for '%s' is not a valid %s.", key, description), e)
        }

    @Throws(ParsingException::class)
    private fun <T> extractOptionalList(json: JsonObject, key: String, description: String, valueExtractor: JsonArrayValueExtractor<T>): List<T?>? =
        try {
            extractOptionalArray(json, key)?.convertToList(valueExtractor)
        } catch (e: ClassCastException) {
            throw ParsingException(String.format("Value for '%s' is not a valid list of %s.", key, description), e)
        }

    @Throws(ParsingException::class)
    fun <T> extractRequired(json: JsonObject, key: String, description: String, valueExtractor: ValueExtractor<T>): T =
        ensureRequired(key, extractOptional(json, key, description, valueExtractor))

    @Throws(ParsingException::class)
    fun <T> extractRequiredList(json: JsonObject, key: String, description: String, valueExtractor: JsonArrayValueExtractor<T>): List<T?> =
        ensureRequired(key, extractOptionalList(json, key, description, valueExtractor))

    @Throws(ParsingException::class)
    fun <T> ensureRequired(key: String, value: T?): T =
        value ?: throw ParsingException(String.format("No value found for required key '%s'.", key))

    private fun <T> JsonArray.convertToList(valueExtractor: JsonArrayValueExtractor<T>): List<T?> =
        (0..<size()).asSequence().map { pos -> valueExtractor(this, pos) }.toList()

    class ParsingException(message: String, cause: Throwable? = null) : Exception(message, cause)

}
