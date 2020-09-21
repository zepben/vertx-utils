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

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@EverythingIsNonnullByDefault
public class LazyJsonArray extends io.vertx.core.json.JsonArray {

    private final Supplier<io.vertx.core.json.JsonArray> supplier;
    private boolean materialised = false;

    public LazyJsonArray(Supplier<io.vertx.core.json.JsonArray> supplier) {
        this.supplier = supplier;
    }

    @Override
    public String getString(int pos) {
        checkLoad();
        return super.getString(pos);
    }

    @Override
    public Integer getInteger(int pos) {
        checkLoad();
        return super.getInteger(pos);
    }

    @Override
    public Long getLong(int pos) {
        checkLoad();
        return super.getLong(pos);
    }

    @Override
    public Double getDouble(int pos) {
        checkLoad();
        return super.getDouble(pos);
    }

    @Override
    public Float getFloat(int pos) {
        checkLoad();
        return super.getFloat(pos);
    }

    @Override
    public Boolean getBoolean(int pos) {
        checkLoad();
        return super.getBoolean(pos);
    }

    @Override
    public JsonObject getJsonObject(int pos) {
        checkLoad();
        return super.getJsonObject(pos);
    }

    @Override
    public JsonArray getJsonArray(int pos) {
        checkLoad();
        return super.getJsonArray(pos);
    }

    @Override
    public byte[] getBinary(int pos) {
        checkLoad();
        return super.getBinary(pos);
    }

    @Override
    public Instant getInstant(int pos) {
        checkLoad();
        return super.getInstant(pos);
    }

    @Override
    public Object getValue(int pos) {
        checkLoad();
        return super.getValue(pos);
    }

    @Override
    public boolean hasNull(int pos) {
        checkLoad();
        return super.hasNull(pos);
    }

    @Override
    public boolean contains(Object value) {
        checkLoad();
        return super.contains(value);
    }

    @Override
    public boolean remove(Object value) {
        checkLoad();
        return super.remove(value);
    }

    @Override
    public Object remove(int pos) {
        checkLoad();
        return super.remove(pos);
    }

    @Override
    public int size() {
        checkLoad();
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        checkLoad();
        return super.isEmpty();
    }

    @Override
    public List<?> getList() {
        checkLoad();
        return super.getList();
    }

    @Override
    public Iterator<Object> iterator() {
        checkLoad();
        return super.iterator();
    }

    @Override
    public String encode() {
        checkLoad();
        return super.encode();
    }

    @Override
    public Buffer toBuffer() {
        checkLoad();
        return super.toBuffer();
    }

    @Override
    public String encodePrettily() {
        checkLoad();
        return super.encodePrettily();
    }

    @Override
    public JsonArray copy() {
        checkLoad();
        return super.copy();
    }

    @Override
    public Stream<Object> stream() {
        checkLoad();
        return super.stream();
    }

    @Override
    public String toString() {
        checkLoad();
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        checkLoad();
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        checkLoad();
        return super.hashCode();
    }

    @Override
    public void writeToBuffer(Buffer buffer) {
        checkLoad();
        super.writeToBuffer(buffer);
    }

    @Override
    public int readFromBuffer(int pos, Buffer buffer) {
        checkLoad();
        return super.readFromBuffer(pos, buffer);
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        checkLoad();
        super.forEach(action);
    }

    @Override
    public Spliterator<Object> spliterator() {
        checkLoad();
        return super.spliterator();
    }

    private void checkLoad() {
        if (!materialised) {
            materialised = true;
            super.addAll(supplier.get());
        }
    }

}
