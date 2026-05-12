/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.testutils.junit.SystemLogExtension;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;

@EverythingIsNonnullByDefault
public class JsonUtilsTest {

    @RegisterExtension
    static SystemLogExtension systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess();

    @RegisterExtension
    static SystemLogExtension systemErr = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess();

    private static final String VALID_KEY = "key";
    private static final String MISSING_KEY = "key2";

    private final int integerVal1 = 111;
    private final int integerVal2 = 222;
    private final int integerVal3 = 333;
    private final double doubleVal1 = 1.11;
    private final double doubleVal2 = 2.22;
    private final String stringVal = "value";
    private final Path pathVal = Paths.get("valid/path");
    private final String illegalPath = "illegal\0path";

    private final JsonObject jsonObjectVal1 = createObjectWithValue(stringVal);
    private final JsonObject jsonObjectVal2 = createObjectWithValue(doubleVal2);
    private final JsonObject jsonObjectVal3 = createObjectWithValue(illegalPath);
    private final JsonArray jsonArrayOfJsonObjects = createArray(jsonObjectVal1, jsonObjectVal2);
    private final JsonArray jsonArrayOfIntegers = createArray(integerVal1, integerVal2, integerVal3);
    private final JsonArray jsonArrayOfMixed = createArray(jsonObjectVal1, integerVal2);

    @Test
    public void extractValue() {
        validateExtractor(JsonUtils.INSTANCE::extractOptionalValue, JsonUtils.INSTANCE::extractRequiredValue, jsonObjectVal1);
        validateExtractor(JsonUtils.INSTANCE::extractOptionalValue, JsonUtils.INSTANCE::extractRequiredValue, integerVal1);
        validateExtractor(JsonUtils.INSTANCE::extractOptionalValue, JsonUtils.INSTANCE::extractRequiredValue, stringVal);
    }

    @Test
    public void extractObject() {
        validateExtractor(JsonUtils.INSTANCE::extractOptionalObject, JsonUtils.INSTANCE::extractRequiredObject, jsonObjectVal1, stringVal, "object");
    }

    @Test
    public void extractArray() {
        validateExtractor(JsonUtils.INSTANCE::extractOptionalArray, JsonUtils.INSTANCE::extractRequiredArray, jsonArrayOfJsonObjects, stringVal, "array");
        validateExtractor(JsonUtils.INSTANCE::extractOptionalArray, JsonUtils.INSTANCE::extractRequiredArray, jsonArrayOfIntegers, stringVal, "array");
    }

    @Test
    public void extractString() {
        validateExtractor(JsonUtils.INSTANCE::extractOptionalString, JsonUtils.INSTANCE::extractRequiredString, stringVal, integerVal1, "string");
    }

    @Test
    public void extractInt() {
        validateExtractor(JsonUtils.INSTANCE::extractOptionalInt, JsonUtils.INSTANCE::extractRequiredInt, integerVal1, stringVal, "integer");
    }

    @Test
    public void extractDouble() {
        validateExtractor(JsonUtils.INSTANCE::extractOptionalDouble, JsonUtils.INSTANCE::extractRequiredDouble, doubleVal1, stringVal, "double");
        validateExtractor(JsonUtils.INSTANCE::extractOptionalDouble, JsonUtils.INSTANCE::extractRequiredDouble, Double.NaN, stringVal, "double");
    }

    @Test
    public void extractPath() {
        validateExtractor(JsonUtils.INSTANCE::extractOptionalPath, JsonUtils.INSTANCE::extractRequiredPath, pathVal, Path::toString, doubleVal2, "path");
        validateExtractor(JsonUtils.INSTANCE::extractOptionalPath, JsonUtils.INSTANCE::extractRequiredPath, pathVal, Path::toString, illegalPath, "path");
    }

    @Test
    public void extractObjectList() {
        List<JsonObject> jsonObjects = Arrays.asList(jsonObjectVal1, jsonObjectVal2);

        validateExtractor(JsonUtils.INSTANCE::extractOptionalObjectList,
            JsonUtils.INSTANCE::extractRequiredObjectList,
            jsonObjects,
            JsonArray::new,
            Arrays.asList(integerVal1, integerVal2),
            "list of objects");

        validateExtractor(JsonUtils.INSTANCE::extractOptionalObjectList,
            JsonUtils.INSTANCE::extractRequiredObjectList,
            jsonObjects,
            JsonArray::new,
            stringVal,
            "array");
    }

    @Test
    public void extractStringList() {
        List<String> strings = Arrays.asList(stringVal, stringVal);

        validateExtractor(JsonUtils.INSTANCE::extractOptionalStringList,
            JsonUtils.INSTANCE::extractRequiredStringList,
            strings,
            JsonArray::new,
            Arrays.asList(integerVal1, integerVal2),
            "list of strings");

        validateExtractor(JsonUtils.INSTANCE::extractOptionalStringList,
            JsonUtils.INSTANCE::extractRequiredStringList,
            strings,
            JsonArray::new,
            stringVal,
            "array");
    }

    @Test
    public void extractIntList() {
        List<Integer> integers = Arrays.asList(integerVal1, integerVal2);

        validateExtractor(JsonUtils.INSTANCE::extractOptionalIntList,
            JsonUtils.INSTANCE::extractRequiredIntList,
            integers,
            JsonArray::new,
            createArray(stringVal, stringVal),
            "list of integers");

        validateExtractor(JsonUtils.INSTANCE::extractOptionalIntList,
            JsonUtils.INSTANCE::extractRequiredIntList,
            integers,
            JsonArray::new,
            stringVal,
            "array");
    }

    @Test
    public void extractDoubleList() {
        List<Double> doubles = Arrays.asList(doubleVal1, doubleVal2, Double.NaN);

        validateExtractor(JsonUtils.INSTANCE::extractOptionalDoubleList,
            JsonUtils.INSTANCE::extractRequiredDoubleList,
            doubles,
            JsonArray::new,
            createArray(stringVal, stringVal),
            "list of doubles");

        validateExtractor(JsonUtils.INSTANCE::extractOptionalDoubleList,
            JsonUtils.INSTANCE::extractRequiredDoubleList,
            doubles,
            JsonArray::new,
            stringVal,
            "array");
    }

    @Test
    public void extractObjectListOfList() {
        List<List<JsonObject>> lists = Arrays.asList(Arrays.asList(jsonObjectVal1, jsonObjectVal2), Collections.singletonList(jsonObjectVal3));

        Function<List<List<JsonObject>>, Object> listsToJsonArray = l -> l
            .stream()
            .map(JsonArray::new)
            .collect(Collector.of(JsonArray::new, JsonArray::add, JsonArray::addAll));

        validateExtractor(JsonUtils.INSTANCE::extractOptionalObjectListOfList,
            JsonUtils.INSTANCE::extractRequiredObjectListOfList,
            lists,
            listsToJsonArray,
            createArray(stringVal, stringVal),
            "list of object lists");

        validateExtractor(JsonUtils.INSTANCE::extractOptionalObjectListOfList,
            JsonUtils.INSTANCE::extractRequiredObjectListOfList,
            lists,
            listsToJsonArray,
            createArray(jsonArrayOfIntegers),
            "list of object lists");
    }

    @Test
    public void convertsJsonArrayToObjectList() {
        assertThat(JsonUtils.INSTANCE.convertToObjectList(jsonArrayOfJsonObjects), contains(jsonObjectVal1, jsonObjectVal2));

        expect(() -> JsonUtils.INSTANCE.convertToObjectList(jsonArrayOfIntegers))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");

        expect(() -> JsonUtils.INSTANCE.convertToObjectList(jsonArrayOfMixed))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");
    }

    @Test
    public void convertsJsonArrayToList() {
        assertThat(JsonUtils.INSTANCE.convertToList(jsonArrayOfJsonObjects, JsonArray::getJsonObject), contains(jsonObjectVal1, jsonObjectVal2));
        assertThat(JsonUtils.INSTANCE.convertToList(jsonArrayOfIntegers, JsonArray::getInteger), contains(integerVal1, integerVal2, integerVal3));
        assertThat(JsonUtils.INSTANCE.convertToList(jsonArrayOfMixed, JsonArray::getValue), contains(jsonObjectVal1, integerVal2));

        expect(() -> JsonUtils.INSTANCE.convertToList(jsonArrayOfJsonObjects, JsonArray::getInteger))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");

        expect(() -> JsonUtils.INSTANCE.convertToList(jsonArrayOfIntegers, JsonArray::getJsonObject))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");

        expect(() -> JsonUtils.INSTANCE.convertToList(jsonArrayOfMixed, JsonArray::getDouble))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");
    }

    @Test
    public void convertsJsonArrayToListWithExpectedCount() {
        assertThat(JsonUtils.INSTANCE.convertToList(jsonArrayOfJsonObjects, JsonArray::getJsonObject, jsonArrayOfJsonObjects.size()), contains(jsonObjectVal1, jsonObjectVal2));
        assertThat(JsonUtils.INSTANCE.convertToList(jsonArrayOfIntegers, JsonArray::getInteger, jsonArrayOfIntegers.size()), contains(integerVal1, integerVal2, integerVal3));
        assertThat(JsonUtils.INSTANCE.convertToList(jsonArrayOfMixed, JsonArray::getValue, jsonArrayOfMixed.size()), contains(jsonObjectVal1, integerVal2));

        expect(() -> JsonUtils.INSTANCE.convertToList(jsonArrayOfJsonObjects, JsonArray::getJsonObject, 3))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("Invalid number of records in list. Expected exactly 3, found 2.");

        expect(() -> JsonUtils.INSTANCE.convertToList(jsonArrayOfIntegers, JsonArray::getJsonObject, 2))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("Invalid number of records in list. Expected exactly 2, found 3.");

        expect(() -> JsonUtils.INSTANCE.convertToList(jsonArrayOfMixed, JsonArray::getInteger))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");
    }

    private JsonObject createObjectWithValue(@Nullable Object value) {
        JsonObject json = new JsonObject();
        if (value != null)
            json.put(VALID_KEY, value);
        else
            json.putNull(VALID_KEY);
        return new JsonObject(json.encode());
    }

    private JsonArray createArray(@Nullable Object... values) {
        JsonArray json = new JsonArray();
        if (values != null)
            Stream.of(values).forEach(json::add);
        else
            json.addNull();
        return new JsonArray(json.encode());
    }

    private <T> void validateExtractor(BiFunction<JsonObject, String, T> optionalValueExtractor,
                                       BiFunction<JsonObject, String, T> requiredValueExtractor,
                                       T expectedValue) {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue, value -> value);
    }

    private <T> void validateExtractor(BiFunction<JsonObject, String, T> optionalValueExtractor,
                                       BiFunction<JsonObject, String, T> requiredValueExtractor,
                                       T expectedValue,
                                       Function<T, Object> valueConverter) {
        JsonObject validObject = createObjectWithValue(valueConverter.apply(expectedValue));

        assertThat(optionalValueExtractor.apply(validObject, VALID_KEY), equalTo(expectedValue));
        assertThat(optionalValueExtractor.apply(validObject, MISSING_KEY), nullValue());

        assertThat(requiredValueExtractor.apply(validObject, VALID_KEY), equalTo(expectedValue));

        expect(() -> {@SuppressWarnings("unused") var unused = requiredValueExtractor.apply(validObject, MISSING_KEY);})
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("No value found for required key 'key2'.");

    }

    private <T> void validateExtractor(BiFunction<JsonObject, String, T> optionalValueExtractor,
                                       BiFunction<JsonObject, String, T> requiredValueExtractor,
                                       T expectedValue,
                                       Object invalidValue,
                                       String description) {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue, value -> value, invalidValue, description);
    }

    private <T> void validateExtractor(BiFunction<JsonObject, String, T> optionalValueExtractor,
                                       BiFunction<JsonObject, String, T> requiredValueExtractor,
                                       T expectedValue,
                                       Function<T, Object> valueConverter,
                                       Object invalidValue,
                                       String description) {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue, valueConverter);

        JsonObject invalidObject = createObjectWithValue(invalidValue);

        expect(() -> {@SuppressWarnings("unused") var unused = optionalValueExtractor.apply(invalidObject, VALID_KEY);})
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage(String.format("Value for 'key' is not a valid %s.", description));

        expect(() -> {@SuppressWarnings("unused") var unused = requiredValueExtractor.apply(invalidObject, VALID_KEY);})
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage(String.format("Value for 'key' is not a valid %s.", description));
    }

}
