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
import java.util.function.Function;
import java.util.stream.Stream;

import static com.zepben.testutils.exception.ExpectException.expect;
import static com.zepben.vertxutils.json.Collectors.toJsonArray;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;

@EverythingIsNonnullByDefault
public class JsonUtilsTest {

    @RegisterExtension
    SystemLogExtension systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess();

    @RegisterExtension
    SystemLogExtension systemErr = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess();

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

    @SuppressWarnings("InstantiationOfUtilityClass")
    @Test
    public void coverage() {
        new JsonUtils();
        new JsonValueExtractors();
    }

    @Test
    public void extractValue() throws Exception {
        validateExtractor(JsonUtils::extractOptionalValue, JsonUtils::extractRequiredValue, jsonObjectVal1);
        validateExtractor(JsonUtils::extractOptionalValue, JsonUtils::extractRequiredValue, integerVal1);
        validateExtractor(JsonUtils::extractOptionalValue, JsonUtils::extractRequiredValue, stringVal);
    }

    @Test
    public void extractObject() throws Exception {
        validateExtractor(JsonUtils::extractOptionalObject, JsonUtils::extractRequiredObject, jsonObjectVal1, stringVal, "object");
    }

    @Test
    public void extractArray() throws Exception {
        validateExtractor(JsonUtils::extractOptionalArray, JsonUtils::extractRequiredArray, jsonArrayOfJsonObjects, stringVal, "array");
        validateExtractor(JsonUtils::extractOptionalArray, JsonUtils::extractRequiredArray, jsonArrayOfIntegers, stringVal, "array");
    }

    @Test
    public void extractString() throws Exception {
        validateExtractor(JsonUtils::extractOptionalString, JsonUtils::extractRequiredString, stringVal, integerVal1, "string");
    }

    @Test
    public void extractInt() throws Exception {
        validateExtractor(JsonUtils::extractOptionalInt, JsonUtils::extractRequiredInt, integerVal1, stringVal, "integer");
    }

    @Test
    public void extractDouble() throws Exception {
        validateExtractor(JsonUtils::extractOptionalDouble, JsonUtils::extractRequiredDouble, doubleVal1, stringVal, "double");
        validateExtractor(JsonUtils::extractOptionalDouble, JsonUtils::extractRequiredDouble, Double.NaN, stringVal, "double");
    }

    @Test
    public void extractPath() throws Exception {
        validateExtractor(JsonUtils::extractOptionalPath, JsonUtils::extractRequiredPath, pathVal, Path::toString, doubleVal2, "path");
        validateExtractor(JsonUtils::extractOptionalPath, JsonUtils::extractRequiredPath, pathVal, Path::toString, illegalPath, "path");
    }

    @Test
    public void extractObjectList() throws Exception {
        List<JsonObject> jsonObjects = Arrays.asList(jsonObjectVal1, jsonObjectVal2);

        validateExtractor(JsonUtils::extractOptionalObjectList,
            JsonUtils::extractRequiredObjectList,
            jsonObjects,
            JsonArray::new,
            Arrays.asList(integerVal1, integerVal2),
            "list of objects");

        validateExtractor(JsonUtils::extractOptionalObjectList,
            JsonUtils::extractRequiredObjectList,
            jsonObjects,
            JsonArray::new,
            stringVal,
            "array");
    }

    @Test
    public void extractStringList() throws Exception {
        List<String> strings = Arrays.asList(stringVal, stringVal);

        validateExtractor(JsonUtils::extractOptionalStringList,
            JsonUtils::extractRequiredStringList,
            strings,
            JsonArray::new,
            Arrays.asList(integerVal1, integerVal2),
            "list of strings");

        validateExtractor(JsonUtils::extractOptionalStringList,
            JsonUtils::extractRequiredStringList,
            strings,
            JsonArray::new,
            stringVal,
            "array");
    }

    @Test
    public void extractIntList() throws Exception {
        List<Integer> integers = Arrays.asList(integerVal1, integerVal2);

        validateExtractor(JsonUtils::extractOptionalIntList,
            JsonUtils::extractRequiredIntList,
            integers,
            JsonArray::new,
            createArray(stringVal, stringVal),
            "list of integers");

        validateExtractor(JsonUtils::extractOptionalIntList,
            JsonUtils::extractRequiredIntList,
            integers,
            JsonArray::new,
            stringVal,
            "array");
    }

    @Test
    public void extractDoubleList() throws Exception {
        List<Double> doubles = Arrays.asList(doubleVal1, doubleVal2, Double.NaN);

        validateExtractor(JsonUtils::extractOptionalDoubleList,
            JsonUtils::extractRequiredDoubleList,
            doubles,
            JsonArray::new,
            createArray(stringVal, stringVal),
            "list of doubles");

        validateExtractor(JsonUtils::extractOptionalDoubleList,
            JsonUtils::extractRequiredDoubleList,
            doubles,
            JsonArray::new,
            stringVal,
            "array");
    }

    @Test
    public void extractObjectListOfList() throws Exception {
        List<List<JsonObject>> lists = Arrays.asList(Arrays.asList(jsonObjectVal1, jsonObjectVal2), Collections.singletonList(jsonObjectVal3));

        Function<List<List<JsonObject>>, Object> listsToJsonArray = l -> l
            .stream()
            .map(JsonArray::new)
            .collect(toJsonArray());

        validateExtractor(JsonUtils::extractOptionalObjectListOfList,
            JsonUtils::extractRequiredObjectListOfList,
            lists,
            listsToJsonArray,
            createArray(stringVal, stringVal),
            "list of object lists");

        validateExtractor(JsonUtils::extractOptionalObjectListOfList,
            JsonUtils::extractRequiredObjectListOfList,
            lists,
            listsToJsonArray,
            createArray(jsonArrayOfIntegers),
            "list of object lists");
    }

    @Test
    public void convertsJsonArrayToObjectList() throws Exception {
        assertThat(JsonUtils.convertToObjectList(jsonArrayOfJsonObjects), contains(jsonObjectVal1, jsonObjectVal2));

        expect(() -> JsonUtils.convertToObjectList(jsonArrayOfIntegers))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");

        expect(() -> JsonUtils.convertToObjectList(jsonArrayOfMixed))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");
    }

    @Test
    public void convertsJsonArrayToList() throws Exception {
        assertThat(JsonUtils.convertToList(jsonArrayOfJsonObjects, JsonArray::getJsonObject), contains(jsonObjectVal1, jsonObjectVal2));
        assertThat(JsonUtils.convertToList(jsonArrayOfIntegers, JsonArray::getInteger), contains(integerVal1, integerVal2, integerVal3));
        assertThat(JsonUtils.convertToList(jsonArrayOfMixed, JsonArray::getValue), contains(jsonObjectVal1, integerVal2));

        expect(() -> JsonUtils.convertToList(jsonArrayOfJsonObjects, JsonArray::getInteger))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");

        expect(() -> JsonUtils.convertToList(jsonArrayOfIntegers, JsonArray::getJsonObject))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");

        expect(() -> JsonUtils.convertToList(jsonArrayOfMixed, JsonArray::getDouble))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("JSON array is not a collection of expected types.");
    }

    @Test
    public void convertsJsonArrayToListWithExpectedCount() throws Exception {
        assertThat(JsonUtils.convertToList(jsonArrayOfJsonObjects, JsonArray::getJsonObject, jsonArrayOfJsonObjects.size()), contains(jsonObjectVal1, jsonObjectVal2));
        assertThat(JsonUtils.convertToList(jsonArrayOfIntegers, JsonArray::getInteger, jsonArrayOfIntegers.size()), contains(integerVal1, integerVal2, integerVal3));
        assertThat(JsonUtils.convertToList(jsonArrayOfMixed, JsonArray::getValue, jsonArrayOfMixed.size()), contains(jsonObjectVal1, integerVal2));

        expect(() -> JsonUtils.convertToList(jsonArrayOfJsonObjects, JsonArray::getJsonObject, 3))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("Invalid number of records in list. Expected exactly 3, found 2.");

        expect(() -> JsonUtils.convertToList(jsonArrayOfIntegers, JsonArray::getJsonObject, 2))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("Invalid number of records in list. Expected exactly 2, found 3.");

        expect(() -> JsonUtils.convertToList(jsonArrayOfMixed, JsonArray::getInteger))
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

    private <T> void validateExtractor(OptionalValueExtractor<T> optionalValueExtractor,
                                       ValueExtractor<T> requiredValueExtractor,
                                       T expectedValue) throws Exception {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue, value -> value);
    }

    private <T> void validateExtractor(OptionalValueExtractor<T> optionalValueExtractor,
                                       ValueExtractor<T> requiredValueExtractor,
                                       T expectedValue,
                                       Function<T, Object> valueConverter) throws Exception {
        JsonObject validObject = createObjectWithValue(valueConverter.apply(expectedValue));

        assertThat(optionalValueExtractor.extract(validObject, VALID_KEY).orElseThrow(AssertionError::new), equalTo(expectedValue));
        assertThat(optionalValueExtractor.extract(validObject, MISSING_KEY).isPresent(), equalTo(false));

        assertThat(requiredValueExtractor.extract(validObject, VALID_KEY), equalTo(expectedValue));

        expect(() -> requiredValueExtractor.extract(validObject, MISSING_KEY))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage("No value found for required key 'key2'.");

    }

    private <T> void validateExtractor(OptionalValueExtractor<T> optionalValueExtractor,
                                       ValueExtractor<T> requiredValueExtractor,
                                       T expectedValue,
                                       Object invalidValue,
                                       String description) throws Exception {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue, value -> value, invalidValue, description);
    }

    private <T> void validateExtractor(OptionalValueExtractor<T> optionalValueExtractor,
                                       ValueExtractor<T> requiredValueExtractor,
                                       T expectedValue,
                                       Function<T, Object> valueConverter,
                                       Object invalidValue,
                                       String description) throws Exception {
        validateExtractor(optionalValueExtractor, requiredValueExtractor, expectedValue, valueConverter);

        JsonObject invalidObject = createObjectWithValue(invalidValue);

        expect(() -> optionalValueExtractor.extract(invalidObject, VALID_KEY))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage(String.format("Value for 'key' is not a valid %s.", description));

        expect(() -> requiredValueExtractor.extract(invalidObject, VALID_KEY))
            .toThrow(JsonUtils.ParsingException.class)
            .withMessage(String.format("Value for 'key' is not a valid %s.", description));
    }

}
