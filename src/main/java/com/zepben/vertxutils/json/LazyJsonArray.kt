/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json

import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.time.Instant
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream

class LazyJsonArray(private val supplier: () -> JsonArray) : JsonArray() {

    private var materialised = false

    override fun getString(pos: Int): String? {
        checkLoad()
        return super.getString(pos)
    }

    override fun getInteger(pos: Int): Int? {
        checkLoad()
        return super.getInteger(pos)
    }

    override fun getLong(pos: Int): Long? {
        checkLoad()
        return super.getLong(pos)
    }

    override fun getDouble(pos: Int): Double? {
        checkLoad()
        return super.getDouble(pos)
    }

    override fun getFloat(pos: Int): Float? {
        checkLoad()
        return super.getFloat(pos)
    }

    override fun getBoolean(pos: Int): Boolean? {
        checkLoad()
        return super.getBoolean(pos)
    }

    override fun getJsonObject(pos: Int): JsonObject? {
        checkLoad()
        return super.getJsonObject(pos)
    }

    override fun getJsonArray(pos: Int): JsonArray? {
        checkLoad()
        return super.getJsonArray(pos)
    }

    override fun getBinary(pos: Int): ByteArray? {
        checkLoad()
        return super.getBinary(pos)
    }

    override fun getInstant(pos: Int): Instant? {
        checkLoad()
        return super.getInstant(pos)
    }

    override fun getValue(pos: Int): Any? {
        checkLoad()
        return super.getValue(pos)
    }

    override fun hasNull(pos: Int): Boolean {
        checkLoad()
        return super.hasNull(pos)
    }

    override operator fun contains(value: Any): Boolean {
        checkLoad()
        return super.contains(value)
    }

    override fun remove(value: Any): Boolean {
        checkLoad()
        return super.remove(value)
    }

    override fun remove(pos: Int): Any {
        checkLoad()
        return super.remove(pos)
    }

    override fun size(): Int {
        checkLoad()
        return super.size()
    }

    override fun isEmpty(): Boolean {
        checkLoad()
        return super.isEmpty()
    }

    override fun getList(): MutableList<*> {
        checkLoad()
        return super.getList()
    }

    override fun iterator(): MutableIterator<Any?> {
        checkLoad()
        return super.iterator()
    }

    override fun encode(): String {
        checkLoad()
        return super.encode()
    }

    override fun toBuffer(): Buffer {
        checkLoad()
        return super.toBuffer()
    }

    override fun encodePrettily(): String {
        checkLoad()
        return super.encodePrettily()
    }

    override fun copy(): JsonArray {
        checkLoad()
        return super.copy()
    }

    override fun stream(): Stream<Any?> {
        checkLoad()
        return super.stream()
    }

    override fun toString(): String {
        checkLoad()
        return super.toString()
    }

    override fun equals(o: Any?): Boolean {
        checkLoad()
        return super.equals(o)
    }

    override fun hashCode(): Int {
        checkLoad()
        return super.hashCode()
    }

    override fun writeToBuffer(buffer: Buffer) {
        checkLoad()
        super.writeToBuffer(buffer)
    }

    override fun readFromBuffer(pos: Int, buffer: Buffer): Int {
        checkLoad()
        return super.readFromBuffer(pos, buffer)
    }

    override fun forEach(action: Consumer<in Any?>) {
        checkLoad()
        super.forEach(action)
    }

    fun forEach(action: (Any?) -> Unit) {
        checkLoad()
        super.forEach(action)
    }

    override fun spliterator(): Spliterator<Any?> {
        checkLoad()
        return super.spliterator()
    }

    private fun checkLoad() {
        if (!materialised) {
            materialised = true
            super.addAll(supplier())
        }
    }

}
