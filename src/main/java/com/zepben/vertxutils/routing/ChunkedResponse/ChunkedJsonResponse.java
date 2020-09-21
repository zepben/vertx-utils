/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.ChunkedResponse;

import com.zepben.annotations.EverythingIsNonnullByDefault;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Fluent Helper to send a JSON response in chunks.
 */
@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public abstract class ChunkedJsonResponse {

    public final static int DEFAULT_BUFFER_SIZE = 1 << 21;

    public class JsonObject {

        private JsonObject() {
        }

        public JsonArray beginArray(String key) {
            if (isNotObject())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open object to begin an array.");

            return doAddKey(key).doBeginArray();
        }

        public JsonObject beginObject(String key) {
            if (isNotObject())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open object to begin another object.");

            return doAddKey(key).doBeginObject();
        }

        public JsonObject addJson(String key, String json) {
            if (isNotObject())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open object to add json.");

            return doAddKey(key).doAddJson(json);
        }

        public JsonObject endObject() {
            if (isNotObject())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open object to end.");

            doEndObject(JsonObjectType.OBJECT);
            return object;
        }

        public JsonArray endObjectInArray() {
            if (isNotObject())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open object to end.");

            doEndObject(JsonObjectType.ARRAY);
            return array;
        }

        @SuppressWarnings("UnusedReturnValue")
        public JsonObject send(boolean force) {
            ChunkedJsonResponse.this.send(force, sb);
            return this;
        }

        private JsonArray doBeginArray() {
            openItems.push(JsonObjectType.ARRAY);
            isFirst = true;

            sb.append("[");

            return array;
        }

        private JsonObject doBeginObject() {
            openItems.push(JsonObjectType.OBJECT);
            isFirst = true;

            sb.append("{");

            return object;
        }

        private JsonObject doAddKey(String key) {
            if (isFirst)
                isFirst = false;
            else
                sb.append(",");

            sb.append("\"").append(key).append("\":");

            return this;
        }

        private JsonObject doAddJson(String json) {
            sb.append(json);
            send(false);

            return this;
        }

        private void doEndObject(JsonObjectType expectedParentType) {
            openItems.pop();
            if (!openItems.isEmpty() && (openItems.peek() != expectedParentType)) {
                openItems.push(JsonObjectType.OBJECT);
                throw new IllegalStateException("INTERNAL ERROR: Incorrect end object method called, the parent is not of the expected type.");
            }

            sb.append("}");
            isFirst = false;

            if (openItems.isEmpty())
                end(sb);
            else
                send(false);
        }

    }

    public class JsonArray {

        private JsonArray() {
        }

        public JsonArray addArrayItem(String json) {
            if (isNotArray())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open array to add json.");

            if (isFirst)
                isFirst = false;
            else
                sb.append(",");

            sb.append(json);
            send(false);

            return this;
        }

        public JsonObject beginObject() {
            if (isNotArray())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open array to begin an object.");

            openItems.push(JsonObjectType.OBJECT);

            if (!isFirst)
                sb.append(",");

            isFirst = true;
            sb.append("{");

            return object;
        }

        public JsonArray beginArray() {
            if (isNotArray())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open array to begin an array.");

            openItems.push(JsonObjectType.ARRAY);

            if (!isFirst)
                sb.append(",");

            isFirst = true;
            sb.append("[");

            return array;
        }

        public JsonObject endArray() {
            if (isNotArray())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open array to end.");

            doEndArray(JsonObjectType.OBJECT);
            return object;
        }

        public JsonArray endArrayInArray() {
            if (isNotArray())
                throw new IllegalStateException("INTERNAL ERROR: You must have an open array to end.");

            doEndArray(JsonObjectType.ARRAY);
            return array;
        }

        @SuppressWarnings("UnusedReturnValue")
        public JsonArray send(boolean force) {
            ChunkedJsonResponse.this.send(force, sb);
            return this;
        }

        private void doEndArray(JsonObjectType expectedParentType) {
            openItems.pop();
            if (!openItems.isEmpty() && (openItems.peek() != expectedParentType)) {
                openItems.push(JsonObjectType.ARRAY);
                throw new IllegalStateException("INTERNAL ERROR: Incorrect end array method called, the parent is not of the expected type.");
            }

            sb.append("]");
            isFirst = false;

            if (openItems.isEmpty())
                end(sb);
            else
                send(false);
        }

    }

    private enum JsonObjectType {OBJECT, ARRAY}


    private final StringBuilder sb;
    private final JsonObject object = new JsonObject();
    private final JsonArray array = new JsonArray();

    boolean isFirst = true;
    private final Deque<JsonObjectType> openItems = new ArrayDeque<>();

    public ChunkedJsonResponse(int bufferSize) {
        sb = new StringBuilder(bufferSize);
    }

    public JsonArray ofArray() {
        if (!openItems.isEmpty())
            throw new IllegalStateException("INTERNAL ERROR: You can only start one object or array for a response.");
        return object.doBeginArray();
    }

    public JsonObject ofObject() {
        if (!openItems.isEmpty())
            throw new IllegalStateException("INTERNAL ERROR: You can only start one object or array for a response.");
        return object.doBeginObject();
    }

    private boolean isNotArray() {
        return openItems.peek() != JsonObjectType.ARRAY;
    }

    private boolean isNotObject() {
        return openItems.peek() != JsonObjectType.OBJECT;
    }

    protected abstract void end(StringBuilder sb);

    protected abstract void send(boolean force, StringBuilder sb);

}
