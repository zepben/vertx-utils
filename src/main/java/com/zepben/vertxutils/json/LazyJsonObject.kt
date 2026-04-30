/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
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

class LazyJsonObject(
    private val jsonObjectSupplier: (() -> JsonObject)? = null,
) : JsonObject() {

    private var materialised = false
    private val fieldSuppliers = mutableMapOf<String, () -> Any?>()

    override fun <T> mapTo(type: Class<T?>): T? {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.mapTo(type)
    }

    override fun getString(key: String): String? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getString(key)
    }

    override fun getInteger(key: String): Int? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getInteger(key)
    }

    override fun getLong(key: String): Long? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getLong(key)
    }

    override fun getDouble(key: String): Double? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getDouble(key)
    }

    override fun getFloat(key: String): Float? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getFloat(key)
    }

    override fun getBoolean(key: String): Boolean? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getBoolean(key)
    }

    override fun getJsonObject(key: String): JsonObject? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getJsonObject(key)
    }

    override fun getJsonArray(key: String): JsonArray? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getJsonArray(key)
    }

    override fun getBinary(key: String): ByteArray? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getBinary(key)
    }

    override fun getInstant(key: String): Instant? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getInstant(key)
    }

    override fun getValue(key: String): Any? {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getValue(key)
    }

    override fun getString(key: String, def: String): String {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getString(key, def)
    }

    override fun getInteger(key: String, def: Int): Int {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getInteger(key, def)
    }

    override fun getLong(key: String, def: Long): Long {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getLong(key, def)
    }

    override fun getDouble(key: String, def: Double): Double {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getDouble(key, def)
    }

    override fun getFloat(key: String, def: Float): Float {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getFloat(key, def)
    }

    override fun getBoolean(key: String, def: Boolean): Boolean {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getBoolean(key, def)
    }

    override fun getJsonObject(key: String, def: JsonObject): JsonObject {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getJsonObject(key, def)
    }

    override fun getJsonArray(key: String, def: JsonArray): JsonArray {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getJsonArray(key, def)
    }

    override fun getBinary(key: String, def: ByteArray): ByteArray {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getBinary(key, def)
    }

    override fun getInstant(key: String, def: Instant): Instant {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getInstant(key, def)
    }

    override fun getValue(key: String, def: Any): Any {
        checkObjectMaterialised()
        checkFieldMaterialised(key)
        return super.getValue(key, def)
    }

    override fun containsKey(key: String): Boolean {
        checkObjectMaterialised()
        return super.containsKey(key) || fieldSuppliers.containsKey(key)
    }

    override fun fieldNames(): Set<String> {
        checkObjectMaterialised()
        return super.fieldNames() + fieldSuppliers.keys
    }

    // Warning: This may return an instance of Supplier
    override fun remove(key: String): Any? {
        checkObjectMaterialised()
        return sequenceOf(fieldSuppliers.remove(key), super.remove(key)).filterNotNull().firstOrNull()
    }

    // Note: if other is a LazyJsonObject, mergeIn will replace Suppliers rather than recursively merging them
    override fun mergeIn(other: JsonObject, depth: Int): JsonObject {
        if (depth < 1) {
            return this
        }

        checkObjectMaterialised()

        if (other is LazyJsonObject) {
            val ljo = other
            other.fieldNames().forEach { f ->
                super.remove(f)
                fieldSuppliers.remove(f)
            }
            fieldSuppliers.putAll(ljo.fieldSuppliers)
        }

        return super.mergeIn(other, depth)
    }

    override fun encode(): String {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.encode()
    }

    override fun encodePrettily(): String {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.encodePrettily()
    }

    override fun toBuffer(): Buffer {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.toBuffer()
    }

    override fun copy(): JsonObject {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.copy()
    }

    override fun getMap(): MutableMap<String, Any?> {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.getMap()
    }

    override fun stream(): Stream<MutableMap.MutableEntry<String, Any>> {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.stream()
    }

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, Any>> {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.iterator()
    }

    override fun size(): Int {
        checkObjectMaterialised()
        return super.size() + fieldSuppliers.size
    }

    override fun isEmpty(): Boolean {
        checkObjectMaterialised()
        return super.isEmpty() && fieldSuppliers.isEmpty()
    }

    override fun toString(): String {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.toString()
    }

    override fun equals(o: Any?): Boolean {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.equals(o)
    }

    override fun hashCode(): Int {
        checkObjectMaterialised()
        return super.hashCode() + fieldSuppliers.hashCode()
    }

    override fun writeToBuffer(buffer: Buffer) {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        super.writeToBuffer(buffer)
    }

    override fun forEach(action: Consumer<in MutableMap.MutableEntry<String, Any?>>) {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        super.forEach(action)
    }

    fun forEach(action: (MutableMap.MutableEntry<String, Any?>) -> Unit) {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        super.forEach(action)
    }

    override fun spliterator(): Spliterator<MutableMap.MutableEntry<String, Any?>> {
        checkObjectMaterialised()
        checkFieldsMaterialised()
        return super.spliterator()
    }

    operator fun set(key: String, supplier: () -> Any?) {
        fieldSuppliers[key] = supplier
    }

    fun put(key: String, supplier: () -> Any?) {
        fieldSuppliers[key] = supplier
    }

    fun lazyPut(key: String, supplier: () -> Any?): LazyJsonObject {
        fieldSuppliers[key] = supplier
        return this
    }

    private fun checkObjectMaterialised() {
        if (!materialised && (jsonObjectSupplier != null)) {
            materialised = true
            mergeIn(jsonObjectSupplier())
        }
    }

    private fun checkFieldsMaterialised() {
        fieldSuppliers.forEach { (key, supplier) -> super.put(key, supplier()) }
        fieldSuppliers.clear()
    }

    private fun checkFieldMaterialised(key: String) {
        fieldSuppliers.remove(key)?.also { supplier -> super.put(key, supplier()) }
    }

}
