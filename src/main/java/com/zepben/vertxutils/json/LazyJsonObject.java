/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@EverythingIsNonnullByDefault
@SuppressWarnings({"WeakerAccess"})
public class LazyJsonObject extends JsonObject {

    @Nullable
    private Supplier<JsonObject> jsonObjectSupplier;
    private boolean materialised = false;
    private final Map<String, Supplier<?>> fieldSuppliers = new HashMap<>();

    public LazyJsonObject(Supplier<JsonObject> supplier) {
        this.jsonObjectSupplier = supplier;
    }

    public LazyJsonObject() {
    }

    @Override
    public <T> T mapTo(Class<T> type) {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.mapTo(type);
    }

    @Override
    public String getString(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getString(key);
    }

    @Override
    public Integer getInteger(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getInteger(key);
    }

    @Override
    public Long getLong(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getLong(key);
    }

    @Override
    public Double getDouble(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getDouble(key);
    }

    @Override
    public Float getFloat(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getFloat(key);
    }

    @Override
    public Boolean getBoolean(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getBoolean(key);
    }

    @Override
    public JsonObject getJsonObject(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getJsonObject(key);
    }

    @Override
    public JsonArray getJsonArray(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getJsonArray(key);
    }

    @Override
    public byte[] getBinary(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getBinary(key);
    }

    @Override
    public Instant getInstant(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getInstant(key);
    }

    @Override
    public Object getValue(String key) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getValue(key);
    }

    @Override
    public String getString(String key, String def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getString(key, def);
    }

    @Override
    public Integer getInteger(String key, Integer def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getInteger(key, def);
    }

    @Override
    public Long getLong(String key, Long def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getLong(key, def);
    }

    @Override
    public Double getDouble(String key, Double def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getDouble(key, def);
    }

    @Override
    public Float getFloat(String key, Float def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getFloat(key, def);
    }

    @Override
    public Boolean getBoolean(String key, Boolean def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getBoolean(key, def);
    }

    @Override
    public JsonObject getJsonObject(String key, JsonObject def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getJsonObject(key, def);
    }

    @Override
    public JsonArray getJsonArray(String key, JsonArray def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getJsonArray(key, def);
    }

    @Override
    public byte[] getBinary(String key, byte[] def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getBinary(key, def);
    }

    @Override
    public Instant getInstant(String key, Instant def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getInstant(key, def);
    }

    @Override
    public Object getValue(String key, Object def) {
        checkObjectMaterialised();
        checkFieldMaterialised(key);
        return super.getValue(key, def);
    }

    @Override
    public boolean containsKey(String key) {
        checkObjectMaterialised();
        return super.containsKey(key) || fieldSuppliers.containsKey(key);
    }

    @Override
    public Set<String> fieldNames() {
        checkObjectMaterialised();
        Set<String> names = new HashSet<>(super.fieldNames());
        names.addAll(fieldSuppliers.keySet());
        return names;
    }

    // Warning: This may return an instance of Supplier
    @Override
    public Object remove(String key) {
        checkObjectMaterialised();
        return ObjectUtils.firstNonNull(fieldSuppliers.remove(key), super.remove(key));
    }

    // Note: if other is a LazyJsonObject, mergeIn will replace Suppliers rather than recursively merging them
    @Override
    public JsonObject mergeIn(JsonObject other, int depth) {
        if (depth < 1) {
            return this;
        }

        checkObjectMaterialised();

        if (other instanceof LazyJsonObject) {
            LazyJsonObject ljo = (LazyJsonObject) other;
            for (String f : other.fieldNames()) {
                super.remove(f);
                fieldSuppliers.remove(f);
            }
            fieldSuppliers.putAll(ljo.fieldSuppliers);
        }

        return super.mergeIn(other, depth);
    }

    @Override
    public String encode() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.encode();
    }

    @Override
    public String encodePrettily() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.encodePrettily();
    }

    @Override
    public Buffer toBuffer() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.toBuffer();
    }

    @Override
    public JsonObject copy() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.copy();
    }

    @Override
    public Map<String, Object> getMap() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.getMap();
    }

    @Override
    public Stream<Map.Entry<String, Object>> stream() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.stream();
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.iterator();
    }

    @Override
    public int size() {
        checkObjectMaterialised();
        return super.size() + fieldSuppliers.size();
    }

    @Override
    public boolean isEmpty() {
        checkObjectMaterialised();
        return super.isEmpty() && fieldSuppliers.isEmpty();
    }

    @Override
    public String toString() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        checkObjectMaterialised();
        return super.hashCode() + fieldSuppliers.hashCode();
    }

    @Override
    public void writeToBuffer(Buffer buffer) {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        super.writeToBuffer(buffer);
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<String, Object>> action) {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        super.forEach(action);
    }

    @Override
    public Spliterator<Map.Entry<String, Object>> spliterator() {
        checkObjectMaterialised();
        checkFieldsMaterialised();
        return super.spliterator();
    }

    public void put(String key, Supplier<?> supplier) {
        fieldSuppliers.put(key, supplier);
    }

    public LazyJsonObject lazyPut(String key, Supplier<?> supplier) {
        fieldSuppliers.put(key, supplier);
        return this;
    }

    private void checkObjectMaterialised() {
        if (!materialised && jsonObjectSupplier != null) {
            materialised = true;
            mergeIn(jsonObjectSupplier.get());
        }
    }

    private void checkFieldsMaterialised() {
        fieldSuppliers.forEach((key, supplier) -> super.put(key, supplier.get()));
        fieldSuppliers.clear();
    }

    private void checkFieldMaterialised(String key) {
        Supplier<?> supplier = fieldSuppliers.remove(key);
        if (supplier != null) {
            super.put(key, supplier.get());
        }
    }

}
