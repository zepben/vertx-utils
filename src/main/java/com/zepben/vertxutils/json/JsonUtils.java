/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class JsonUtils {

    @EverythingIsNonnullByDefault
    public static class ParsingException extends Exception {

        public ParsingException(String message) {
            super(message);
        }

        public ParsingException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * Get the value from the specified key.
     *
     * @param json json object to extract the value from
     * @param key  the key containing the value
     * @return optional of the value
     */
    public static Optional<Object> extractOptionalValue(JsonObject json, String key) {
        // NOTE: We don't use the generic extract optional here to avoid having the
        //       un-throwable checked exception added to the signature.
        return Optional.ofNullable(json.getValue(key));
    }

    /**
     * Get the value from the specified key.
     *
     * @param json json object to extract the value from
     * @param key  the key containing the value
     * @return the value
     * @throws ParsingException if the key is not found
     */
    public static Object extractRequiredValue(JsonObject json, String key) throws ParsingException {
        return extractRequired(json, key, JsonObject::getValue, "value");
    }

    /**
     * Get the object value from the specified key.
     *
     * @param json json object to extract the object from
     * @param key  the key containing the object
     * @return optional of the value
     * @throws ParsingException if the value is not an object
     */
    public static Optional<JsonObject> extractOptionalObject(JsonObject json, String key) throws ParsingException {
        return extractOptional(json, key, JsonObject::getJsonObject, "object");
    }

    /**
     * Get the object value from the specified key.
     *
     * @param json json object to extract the object from
     * @param key  the key containing the object
     * @return the value
     * @throws ParsingException if the value is not an object, or is not found
     */
    public static JsonObject extractRequiredObject(JsonObject json, String key) throws ParsingException {
        return extractRequired(json, key, JsonObject::getJsonObject, "object");
    }

    /**
     * Get the array value from the specified key.
     *
     * @param json json object to extract the array from
     * @param key  the key containing the array
     * @return optional of the value
     * @throws ParsingException if the value is not an array
     */
    public static Optional<JsonArray> extractOptionalArray(JsonObject json, String key) throws ParsingException {
        return extractOptional(json, key, JsonObject::getJsonArray, "array");
    }

    /**
     * Get the array value from the specified key.
     *
     * @param json json object to extract the array from
     * @param key  the key containing the array
     * @return the value
     * @throws ParsingException if the value is not an array, or is not found
     */
    public static JsonArray extractRequiredArray(JsonObject json, String key) throws ParsingException {
        return extractRequired(json, key, JsonObject::getJsonArray, "array");
    }

    /**
     * Get the string value from the specified key.
     *
     * @param json json object to extract the string from
     * @param key  the key containing the string
     * @return optional of the value
     * @throws ParsingException if the value is not a string
     */
    public static Optional<String> extractOptionalString(JsonObject json, String key) throws ParsingException {
        return extractOptional(json, key, JsonObject::getString, "string");
    }

    /**
     * Get the string value from the specified key.
     *
     * @param json json object to extract the string from
     * @param key  the key containing the string
     * @return the value
     * @throws ParsingException if the value is not a string, or is not found
     */
    public static String extractRequiredString(JsonObject json, String key) throws ParsingException {
        return extractRequired(json, key, JsonObject::getString, "string");
    }

    /**
     * Get the integer value from the specified key.
     *
     * @param json json object to extract the integer from
     * @param key  the key containing the integer
     * @return optional of the value
     * @throws ParsingException if the value is not an integer
     */
    public static Optional<Integer> extractOptionalInt(JsonObject json, String key) throws ParsingException {
        return extractOptional(json, key, JsonObject::getInteger, "integer");
    }

    /**
     * Get the integer value from the specified key.
     *
     * @param json json object to extract the integer from
     * @param key  the key containing the integer
     * @return the value
     * @throws ParsingException if the value is not an integer, or is not found
     */
    public static int extractRequiredInt(JsonObject json, String key) throws ParsingException {
        return extractRequired(json, key, JsonObject::getInteger, "integer");
    }

    /**
     * Get the double value from the specified key.
     *
     * @param json json object to extract the double from
     * @param key  the key containing the double
     * @return optional of the value
     * @throws ParsingException if the value is not a double
     */
    public static Optional<Double> extractOptionalDouble(JsonObject json, String key) throws ParsingException {
        return extractOptional(json, key, JsonValueExtractors::getDouble, "double");
    }

    /**
     * Get the double value from the specified key.
     *
     * @param json json object to extract the double from
     * @param key  the key containing the double
     * @return the value
     * @throws ParsingException if the value is not a double, or is not found
     */
    public static double extractRequiredDouble(JsonObject json, String key) throws ParsingException {
        return extractRequired(json, key, JsonValueExtractors::getDouble, "double");
    }

    /**
     * Get the path value from the specified key.
     *
     * @param json json object to extract the path from
     * @param key  the key containing the path
     * @return optional of the value
     * @throws ParsingException if the value is not a path
     */
    public static Optional<Path> extractOptionalPath(JsonObject json, String key) throws ParsingException {
        return extractOptional(json, key, JsonValueExtractors::getPath, "path");
    }

    /**
     * Get the path value from the specified key.
     *
     * @param json json object to extract the path from
     * @param key  the key containing the path
     * @return the value
     * @throws ParsingException if the value is not a path, or is not found
     */
    public static Path extractRequiredPath(JsonObject json, String key) throws ParsingException {
        return extractRequired(json, key, JsonValueExtractors::getPath, "path");
    }

    /**
     * Get the JsonObjects from a JsonArray with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the JsonObjects contained in the specified array.
     * @throws ParsingException if the value is not a list of objects.
     */
    public static Optional<List<JsonObject>> extractOptionalObjectList(JsonObject json, String key) throws ParsingException {
        return extractOptionalList(json, key, JsonArray::getJsonObject, "objects");
    }

    /**
     * Get the JsonObjects from a JsonArray with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the JsonObjects contained in the specified array.
     * @throws ParsingException if the value is not a list of objects, or is not found.
     */
    public static List<JsonObject> extractRequiredObjectList(JsonObject json, String key) throws ParsingException {
        return extractRequiredList(json, key, JsonArray::getJsonObject, "objects");
    }

    /**
     * Get the strings from a JsonArray with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the strings contained in the specified array.
     * @throws ParsingException if the value is not a list of strings.
     */
    public static Optional<List<String>> extractOptionalStringList(JsonObject json, String key) throws ParsingException {
        return extractOptionalList(json, key, JsonArray::getString, "strings");
    }

    /**
     * Get the strings from a JsonArray with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the strings contained in the specified array.
     * @throws ParsingException if the value is not a list of strings, or is not found.
     */
    public static List<String> extractRequiredStringList(JsonObject json, String key) throws ParsingException {
        return extractRequiredList(json, key, JsonArray::getString, "strings");
    }

    /**
     * Get the integers from a JsonArray with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the integers contained in the specified array.
     * @throws ParsingException if the value is not a list of integers.
     */
    public static Optional<List<Integer>> extractOptionalIntList(JsonObject json, String key) throws ParsingException {
        return extractOptionalList(json, key, JsonArray::getInteger, "integers");
    }

    /**
     * Get the integers from a JsonArray with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the integers contained in the specified array.
     * @throws ParsingException if the value is not a list of integers, or is not found.
     */
    public static List<Integer> extractRequiredIntList(JsonObject json, String key) throws ParsingException {
        return extractRequiredList(json, key, JsonArray::getInteger, "integers");
    }

    /**
     * Get the doubles from a JsonArray with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list containing each of the doubles contained in the specified array.
     * @throws ParsingException if the value is not a list of doubles.
     */
    public static Optional<List<Double>> extractOptionalDoubleList(JsonObject json, String key) throws ParsingException {
        return extractOptionalList(json, key, JsonValueExtractors::getDouble, "doubles");
    }

    /**
     * Get the doubles from a JsonArray with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list containing each of the doubles contained in the specified array.
     * @throws ParsingException if the value is not a list of doubles, or is not found.
     */
    public static List<Double> extractRequiredDoubleList(JsonObject json, String key) throws ParsingException {
        return extractRequiredList(json, key, JsonValueExtractors::getDouble, "doubles");
    }

    /**
     * Get the JsonObjects from a JsonArray of JsonArrays with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return optional of a list of lists containing each of the JsonObjects contained in the specified array or arrays.
     * @throws ParsingException if the value is not an object list.
     */
    public static Optional<List<List<JsonObject>>> extractOptionalObjectListOfList(JsonObject json, String key) throws ParsingException {
        try {
            return extractOptionalList(json, key, JsonArray::getJsonArray, "object lists")
                .map(jsonArrays -> jsonArrays
                    .stream()
                    .map(ja -> uncheckedConvertToList(ja, JsonArray::getJsonObject))
                    .collect(Collectors.toList()));
        } catch (ClassCastException e) {
            throw new ParsingException(String.format("Value for '%s' is not a valid list of object lists.", key), e);
        }
    }

    /**
     * Get the JsonObjects from a JsonArray of JsonArrays with the specified key as a list.
     *
     * @param json the object containing the array.
     * @param key  the key of the array in the object.
     * @return a list of lists containing each of the JsonObjects contained in the specified array or arrays.
     * @throws ParsingException if the value is not an object list.
     */
    public static List<List<JsonObject>> extractRequiredObjectListOfList(JsonObject json, String key) throws ParsingException {
        Optional<List<List<JsonObject>>> lists = extractOptionalObjectListOfList(json, key);
        return ensureRequired(key, lists::isPresent, lists::get);
    }

    /**
     * @param jsonArray the array containing the objects.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    public static List<JsonObject> convertToObjectList(JsonArray jsonArray) throws ParsingException {
        return convertToList(jsonArray, JsonArray::getJsonObject);
    }

    /**
     * @param jsonArray      the array containing the objects.
     * @param valueExtractor the method used to extract the value from the array.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    public static <T> List<T> convertToList(JsonArray jsonArray, JsonArrayValueExtractor<T> valueExtractor) throws ParsingException {
        try {
            return uncheckedConvertToList(jsonArray, valueExtractor);
        } catch (ClassCastException e) {
            throw new ParsingException("JSON array is not a collection of expected types.", e);
        }
    }

    /**
     * @param jsonArray      the array containing the objects.
     * @param valueExtractor the method used to extract the value from the array.
     * @param expectedCount  the expected number of entries in the list.
     * @return a list of the objects contained in the array.
     * @throws ParsingException if the array does not contain objects.
     */
    public static <T> List<T> convertToList(JsonArray jsonArray, JsonArrayValueExtractor<T> valueExtractor, int expectedCount) throws ParsingException {
        if (jsonArray.size() != expectedCount)
            throw new ParsingException(String.format("Invalid number of records in list. Expected exactly %d, found %d.", expectedCount, jsonArray.size()));

        return convertToList(jsonArray, valueExtractor);
    }

    private static <T> Optional<T> extractOptional(JsonObject json,
                                                   String key,
                                                   ValueExtractor<T> valueExtractor,
                                                   String description) throws ParsingException {
        try {
            return Optional.ofNullable(valueExtractor.extract(json, key));
        } catch (Exception e) {
            throw new ParsingException(String.format("Value for '%s' is not a valid %s.", key, description), e);
        }
    }

    private static <T> Optional<List<T>> extractOptionalList(JsonObject json,
                                                             String key,
                                                             JsonArrayValueExtractor<T> valueExtractor,
                                                             String description) throws ParsingException {
        try {
            return Optional.ofNullable(uncheckedConvertToList(extractOptionalArray(json, key).orElse(null), valueExtractor));
        } catch (ClassCastException e) {
            throw new ParsingException(String.format("Value for '%s' is not a valid list of %s.", key, description), e);
        }
    }

    public static <T> T extractRequired(JsonObject json,
                                        String key,
                                        ValueExtractor<T> valueExtractor,
                                        String description) throws ParsingException {
        Optional<T> value = extractOptional(json, key, valueExtractor, description);
        return ensureRequired(key, value::isPresent, value::get);
    }

    public static <T> List<T> extractRequiredList(JsonObject json,
                                                  String key,
                                                  JsonArrayValueExtractor<T> valueExtractor,
                                                  String description) throws ParsingException {
        Optional<List<T>> value = extractOptionalList(json, key, valueExtractor, description);
        return ensureRequired(key, value::isPresent, value::get);
    }

    public static <T> T ensureRequired(String key, Supplier<Boolean> isPresentSupplier, Supplier<T> valueSupplier) throws ParsingException {
        if (!isPresentSupplier.get())
            throw new ParsingException(String.format("No value found for required key '%s'.", key));

        return valueSupplier.get();
    }

    @Nullable
    @Contract("null, _ -> null; !null, _ -> !null")
    private static <T> List<T> uncheckedConvertToList(@Nullable JsonArray jsonArray, JsonArrayValueExtractor<T> valueExtractor) {
        if (jsonArray != null)
            return IntStream.range(0, jsonArray.size()).mapToObj(i -> valueExtractor.extractValue(jsonArray, i)).collect(Collectors.toList());
        else
            return null;
    }

}
