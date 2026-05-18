/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.json.JsonUtils.convertToList
import com.zepben.vertxutils.json.JsonUtils.convertToObjectList
import com.zepben.vertxutils.json.JsonUtils.extractOptionalArray
import com.zepben.vertxutils.json.JsonUtils.extractOptionalDouble
import com.zepben.vertxutils.json.JsonUtils.extractOptionalDoubleList
import com.zepben.vertxutils.json.JsonUtils.extractOptionalInt
import com.zepben.vertxutils.json.JsonUtils.extractOptionalIntList
import com.zepben.vertxutils.json.JsonUtils.extractOptionalObject
import com.zepben.vertxutils.json.JsonUtils.extractOptionalObjectList
import com.zepben.vertxutils.json.JsonUtils.extractOptionalObjectListOfList
import com.zepben.vertxutils.json.JsonUtils.extractOptionalPath
import com.zepben.vertxutils.json.JsonUtils.extractOptionalString
import com.zepben.vertxutils.json.JsonUtils.extractOptionalStringList
import com.zepben.vertxutils.json.JsonUtils.extractOptionalValue
import com.zepben.vertxutils.json.JsonUtils.extractRequiredArray
import com.zepben.vertxutils.json.JsonUtils.extractRequiredDouble
import com.zepben.vertxutils.json.JsonUtils.extractRequiredDoubleList
import com.zepben.vertxutils.json.JsonUtils.extractRequiredInt
import com.zepben.vertxutils.json.JsonUtils.extractRequiredIntList
import com.zepben.vertxutils.json.JsonUtils.extractRequiredObject
import com.zepben.vertxutils.json.JsonUtils.extractRequiredObjectList
import com.zepben.vertxutils.json.JsonUtils.extractRequiredObjectListOfList
import com.zepben.vertxutils.json.JsonUtils.extractRequiredPath
import com.zepben.vertxutils.json.JsonUtils.extractRequiredString
import com.zepben.vertxutils.json.JsonUtils.extractRequiredStringList
import com.zepben.vertxutils.json.JsonUtils.extractRequiredValue
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.nio.file.Path
import java.nio.file.Paths

class JsonUtilsTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

        private const val VALID_KEY = "key"
        private const val MISSING_KEY = "key2"

    }

    private val integerVal1 = 111
    private val integerVal2 = 222
    private val integerVal3 = 333
    private val doubleVal1 = 1.11
    private val doubleVal2 = 2.22
    private val stringVal = "value"
    private val pathVal: Path = Paths.get("valid/path")
    private val illegalPath = "illegal\u0000path"

    private val jsonObjectVal1: JsonObject = createObjectWithValue(stringVal)
    private val jsonObjectVal2: JsonObject = createObjectWithValue(doubleVal2)
    private val jsonObjectVal3: JsonObject = createObjectWithValue(illegalPath)
    private val jsonArrayOfJsonObjects: JsonArray = createArray(jsonObjectVal1, jsonObjectVal2)
    private val jsonArrayOfIntegers: JsonArray = createArray(integerVal1, integerVal2, integerVal3)
    private val jsonArrayOfMixed: JsonArray = createArray(jsonObjectVal1, integerVal2)

    @Test
    fun extractValue() {
        validateExtractor(
            { obj, json -> obj.extractOptionalValue(json) },
            { obj, json -> obj.extractRequiredValue(json) },
            jsonObjectVal1,
        )
        validateExtractor(
            { obj, json -> obj.extractOptionalValue(json) },
            { obj, json -> obj.extractRequiredValue(json) },
            integerVal1,
        )
        validateExtractor(
            { obj, json -> obj.extractOptionalValue(json) },
            { obj, json -> obj.extractRequiredValue(json) },
            stringVal,
        )
    }

    @Test
    fun extractObject() {
        validateExtractor(
            { obj, json -> obj.extractOptionalObject(json) },
            { obj, json -> obj.extractRequiredObject(json) },
            jsonObjectVal1,
            stringVal,
            "object",
        )
    }

    @Test
    fun extractArray() {
        validateExtractor(
            { obj, json -> obj.extractOptionalArray(json) },
            { obj, json -> obj.extractRequiredArray(json) },
            jsonArrayOfJsonObjects,
            stringVal,
            "array",
        )
        validateExtractor(
            { obj, json -> obj.extractOptionalArray(json) },
            { obj, json -> obj.extractRequiredArray(json) },
            jsonArrayOfIntegers,
            stringVal,
            "array",
        )
    }

    @Test
    fun extractString() {
        validateExtractor(
            { obj, json -> obj.extractOptionalString(json) },
            { obj, json -> obj.extractRequiredString(json) },
            stringVal,
            integerVal1,
            "string",
        )
    }

    @Test
    fun extractInt() {
        validateExtractor(
            { obj, json -> obj.extractOptionalInt(json) },
            { obj, json -> obj.extractRequiredInt(json) },
            integerVal1,
            stringVal,
            "integer",
        )
    }

    @Test
    fun extractDouble() {
        validateExtractor(
            { obj, json -> obj.extractOptionalDouble(json) },
            { obj, json -> obj.extractRequiredDouble(json) },
            doubleVal1,
            stringVal,
            "double",
        )
        validateExtractor(
            { obj, json -> obj.extractOptionalDouble(json) },
            { obj, json -> obj.extractRequiredDouble(json) },
            Double.NaN,
            stringVal,
            "double",
        )
    }

    @Test
    fun extractPath() {
        validateExtractor(
            { obj, json -> obj.extractOptionalPath(json) },
            { obj, json -> obj.extractRequiredPath(json) },
            pathVal,
            doubleVal2,
            "path",
        ) { it.toString() }
        validateExtractor(
            { obj, json -> obj.extractOptionalPath(json) },
            { obj, json -> obj.extractRequiredPath(json) },
            pathVal,
            illegalPath,
            "path",
        ) { it.toString() }
    }

    @Test
    fun extractObjectList() {
        val jsonObjects = listOf(jsonObjectVal1, jsonObjectVal2)

        validateExtractor(
            { obj, json -> obj.extractOptionalObjectList(json) },
            { obj, json -> obj.extractRequiredObjectList(json) },
            jsonObjects,
            listOf(integerVal1, integerVal2),
            "list of objects",
        ) { list -> JsonArray(list) }

        validateExtractor(
            { obj, json -> obj.extractOptionalObjectList(json) },
            { obj, json -> obj.extractRequiredObjectList(json) },
            jsonObjects,
            stringVal,
            "array",
        ) { list -> JsonArray(list) }
    }

    @Test
    fun extractStringList() {
        val strings = listOf(stringVal, stringVal)

        validateExtractor(
            { obj, json -> obj.extractOptionalStringList(json) },
            { obj, json -> obj.extractRequiredStringList(json) },
            strings,
            listOf(integerVal1, integerVal2),
            "list of strings",
        ) { list -> JsonArray(list) }

        validateExtractor(
            { obj, json -> obj.extractOptionalStringList(json) },
            { obj, json -> obj.extractRequiredStringList(json) },
            strings,
            stringVal,
            "array",
        ) { list -> JsonArray(list) }
    }

    @Test
    fun extractIntList() {
        val integers = listOf(integerVal1, integerVal2)

        validateExtractor(
            { obj, json -> obj.extractOptionalIntList(json) },
            { obj, json -> obj.extractRequiredIntList(json) },
            integers,
            createArray(stringVal, stringVal),
            "list of integers",
        ) { list -> JsonArray(list) }

        validateExtractor(
            { obj, json -> obj.extractOptionalIntList(json) },
            { obj, json -> obj.extractRequiredIntList(json) },
            integers,
            stringVal,
            "array",
        ) { list -> JsonArray(list) }
    }

    @Test
    fun extractDoubleList() {
        val doubles = listOf(doubleVal1, doubleVal2, Double.NaN)

        validateExtractor(
            { obj, json -> obj.extractOptionalDoubleList(json) },
            { obj, json -> obj.extractRequiredDoubleList(json) },
            doubles,
            createArray(stringVal, stringVal),
            "list of doubles",
        ) { list -> JsonArray(list) }

        validateExtractor(
            { obj, json -> obj.extractOptionalDoubleList(json) },
            { obj, json -> obj.extractRequiredDoubleList(json) },
            doubles,
            stringVal,
            "array",
        ) { list -> JsonArray(list) }
    }

    @Test
    fun extractObjectListOfList() {
        val lists = listOf(listOf(jsonObjectVal1, jsonObjectVal2), listOf(jsonObjectVal3))

        validateExtractor(
            { obj, json -> obj.extractOptionalObjectListOfList(json) },
            { obj, json -> obj.extractRequiredObjectListOfList(json) },
            lists,
            createArray(stringVal, stringVal),
            "list of object lists",
        ) { list -> JsonArray(list!!.map { JsonArray(it) }) }

        validateExtractor(
            { obj, json -> obj.extractOptionalObjectListOfList(json) },
            { obj, json -> obj.extractRequiredObjectListOfList(json) },
            lists,
            createArray(jsonArrayOfIntegers),
            "list of object lists",
        ) { list -> JsonArray(list!!.map { JsonArray(it) }) }
    }

    @Test
    fun convertsJsonArrayToObjectList() {
        assertThat(
            jsonArrayOfJsonObjects.convertToObjectList(),
            contains(jsonObjectVal1, jsonObjectVal2),
        )

        expect { jsonArrayOfIntegers.convertToObjectList() }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("JSON array is not a collection of expected types.")

        expect { jsonArrayOfMixed.convertToObjectList() }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("JSON array is not a collection of expected types.")
    }

    @Test
    fun convertsJsonArrayToList() {
        assertThat(
            jsonArrayOfJsonObjects.convertToList { obj, pos -> obj.getJsonObject(pos) },
            contains(jsonObjectVal1, jsonObjectVal2),
        )
        assertThat(
            jsonArrayOfIntegers.convertToList { obj, pos -> obj.getInteger(pos) },
            contains(integerVal1, integerVal2, integerVal3),
        )
        assertThat(
            jsonArrayOfMixed.convertToList { obj, pos -> obj.getValue(pos) },
            contains(jsonObjectVal1, integerVal2),
        )

        expect { jsonArrayOfJsonObjects.convertToList { obj, pos -> obj.getInteger(pos) } }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("JSON array is not a collection of expected types.")

        expect { jsonArrayOfIntegers.convertToList { obj, pos -> obj.getJsonObject(pos) } }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("JSON array is not a collection of expected types.")

        expect { jsonArrayOfMixed.convertToList { obj, pos -> obj.getDouble(pos) } }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("JSON array is not a collection of expected types.")
    }

    @Test
    fun convertsJsonArrayToListWithExpectedCount() {
        assertThat(
            jsonArrayOfJsonObjects.convertToList(
                jsonArrayOfJsonObjects.size(),
            ) { arr, pos -> arr.getJsonObject(pos) },
            contains(jsonObjectVal1, jsonObjectVal2),
        )
        assertThat(
            jsonArrayOfIntegers.convertToList(jsonArrayOfIntegers.size()) { obj, pos -> obj.getInteger(pos) },
            contains(integerVal1, integerVal2, integerVal3),
        )
        assertThat(
            jsonArrayOfMixed.convertToList(jsonArrayOfMixed.size()) { obj, pos -> obj.getValue(pos) },
            contains(jsonObjectVal1, integerVal2),
        )

        expect { jsonArrayOfJsonObjects.convertToList(3) { obj, pos -> obj.getJsonObject(pos) } }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("Invalid number of records in list. Expected exactly 3, found 2.")

        expect { jsonArrayOfIntegers.convertToList(2) { obj, pos -> obj.getJsonObject(pos) } }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("Invalid number of records in list. Expected exactly 2, found 3.")

        expect { jsonArrayOfMixed.convertToList { obj, pos -> obj.getInteger(pos) } }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("JSON array is not a collection of expected types.")
    }

    private fun createObjectWithValue(value: Any?): JsonObject =
        json {
            obj(VALID_KEY to value)
        }

    private fun createArray(vararg values: Any?): JsonArray =
        json {
            array(*values)
        }

    private fun <T> validateExtractor(
        optionalValueExtractor: (JsonObject, String) -> T?,
        requiredValueExtractor: (JsonObject, String) -> T,
        expectedValue: T?,
    ) {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue) { it }
    }

    private fun <T> validateExtractor(
        optionalValueExtractor: (JsonObject, String) -> T?,
        requiredValueExtractor: (JsonObject, String) -> T,
        expectedValue: T?,
        valueConverter: (T?) -> Any?,
    ) {
        val validObject = createObjectWithValue(valueConverter(expectedValue))

        assertThat(optionalValueExtractor(validObject, VALID_KEY), equalTo(expectedValue))
        assertThat(optionalValueExtractor(validObject, MISSING_KEY), nullValue())

        assertThat(requiredValueExtractor(validObject, VALID_KEY), equalTo(expectedValue))

        expect { requiredValueExtractor(validObject, MISSING_KEY) }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("No value found for required key 'key2'.")
    }

    private fun <T> validateExtractor(
        optionalValueExtractor: (JsonObject, String) -> T?,
        requiredValueExtractor: (JsonObject, String) -> T,
        expectedValue: T?,
        invalidValue: Any,
        description: String,
    ) {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue, invalidValue, description) { it }
    }

    private fun <T> validateExtractor(
        optionalValueExtractor: (JsonObject, String) -> T?,
        requiredValueExtractor: (JsonObject, String) -> T,
        expectedValue: T?,
        invalidValue: Any,
        description: String,
        valueConverter: (T?) -> Any?,
    ) {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue, valueConverter)

        val invalidObject = createObjectWithValue(invalidValue)

        expect { optionalValueExtractor(invalidObject, VALID_KEY) }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("Value for 'key' is not a valid $description.")

        expect { requiredValueExtractor(invalidObject, VALID_KEY) }
            .toThrow<JsonUtils.ParsingException>()
            .withMessage("Value for 'key' is not a valid $description.")
    }

}
