/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.chunked

import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

/**
 * The base class for building chunked JSON responses.
 *
 * @param bufferSize The initial capacity of the underlying string builder.
 */
abstract class ChunkedJsonResponse(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
) {

    companion object {

        const val DEFAULT_BUFFER_SIZE = 1 shl 21

    }

    private val sb = StringBuilder(bufferSize)
    private val needsSeparatorStack = ArrayDeque<Boolean>()

    /**
     * DSL entry point for building a JSON object response.
     *
     * @param block The DSL block used to populate the object contents.
     */
    fun ofObject(block: JsonObjectBuilder.() -> Unit) {
        check(sb.isEmpty()) { "Can't reuse a non-clean response builder" }
        JsonObjectBuilder().build(block)
    }

    /**
     * DSL entry point for building a JSON array response.
     *
     * @param block The DSL block used to populate the array contents.
     */
    fun ofArray(block: JsonArrayBuilder.() -> Unit) {
        check(sb.isEmpty()) { "Can't reuse a non-clean response builder" }
        JsonArrayBuilder().build(block)
    }

    /**
     * A DSL builder for a JSON object.
     */
    inner class JsonObjectBuilder : JsonBuilder<JsonObjectBuilder>("{", "}") {

        /**
         * Add a scalar field to the JSON object being built, with appropriate escaping.
         *
         * NOTE: This deliberately removes support for adding JsonObject and JsonArray values directly, they should be provided via DSL builders.
         *
         * @param key The key for the filed in the object.
         * @param value The value to associate with the [key].
         * @throws IllegalArgumentException for any unsupported value types.
         */
        fun field(key: String, value: Any?) {
            sb.writeKey(key).appendJsonValue(value)
            checkWrite(sb)
        }

        /**
         * Add a nested object to the JSON object being built.
         *
         * @param key The key for the nested object in the object.
         * @param block The DSL block used to build the nested object.
         */
        fun obj(key: String, block: JsonObjectBuilder.() -> Unit) {
            sb.writeKey(key)
            JsonObjectBuilder().build(block)
        }

        /**
         * Add a nested array to the JSON object being built.
         *
         * @param key The key for the nested array in the object.
         * @param block The DSL block used to build the nested array.
         */
        fun array(key: String, block: JsonArrayBuilder.() -> Unit) {
            sb.writeKey(key)
            JsonArrayBuilder().build(block)
        }

        private fun StringBuilder.writeKey(key: String): StringBuilder =
            maybeAppendSeparator()
                .append(Json.encode(key))
                .append(":")

    }

    /**
     * A DSL builder for a JSON array.
     */
    inner class JsonArrayBuilder : JsonBuilder<JsonArrayBuilder>("[", "]") {

        /**
         * Add a scalar item to the JSON array being built, with appropriate escaping.
         *
         * NOTE: This deliberately removes support for adding JsonObject and JsonArray values directly, they should be provided via DSL builders.
         *
         * @param value The value to add to the array.
         * @throws IllegalArgumentException for any unsupported value types.
         */
        fun item(value: Any?) {
            sb.maybeAppendSeparator().appendJsonValue(value)
            checkWrite(sb)
        }

        /**
         * Add a nested object to the JSON array being built.
         *
         * @param block The DSL block used to build the nested object.
         */
        fun obj(block: JsonObjectBuilder.() -> Unit) {
            sb.maybeAppendSeparator()
            JsonObjectBuilder().build(block)
        }

        /**
         * Add a nested array to the JSON array being built.
         *
         * @param block The DSL block used to build the nested array.
         */
        fun array(block: JsonArrayBuilder.() -> Unit) {
            sb.maybeAppendSeparator()
            JsonArrayBuilder().build(block)
        }

    }

    /**
     * A base class for the DSL builders.
     *
     * @param openToken The token used to open the element being built by this builder.
     * @param openToken The token used to close the element being built by this builder.
     */
    abstract inner class JsonBuilder<B : JsonBuilder<B>>(
        private val openToken: String,
        private val closeToken: String,
    ) {

        /**
         * Write the current buffer to the underlying destination if it is appropriate (e.g. exceeds minimum size requirements), or if it is forced.
         *
         * @param force Flag to indicate that writing should occur without performing any other checks.
         */
        fun checkWrite(force: Boolean = false) = checkWrite(sb, force)

        /**
         * Build the element with the given DSL block.
         *
         * @param block The DSL block used to populate this element.
         */
        internal fun build(block: B.() -> Unit) {
            sb.append(openToken)
            needsSeparatorStack.addLast(false)

            @Suppress("UNCHECKED_CAST")
            (this as B).block()

            sb.append(closeToken)
            needsSeparatorStack.removeLast()

            if (needsSeparatorStack.isEmpty())
                onResponseCompleted(sb)
            else
                checkWrite(sb)
        }

        /**
         * Append a separator if it is required before adding the next element.
         *
         * The first item added to any element will suppress the separator, with any subsequent elements inserting it.
         */
        protected fun StringBuilder.maybeAppendSeparator(): StringBuilder {
            if (needsSeparatorStack.isNotEmpty()) {
                if (needsSeparatorStack.removeLast())
                    append(",")
                needsSeparatorStack.addLast(true)
            }
            return this
        }

        /**
         * Append a JSON value to the element. This will provide required escaping.
         *
         * @param value The scalar value to add to this element.
         * @throws IllegalArgumentException for any unsupported value types.
         */
        protected fun StringBuilder.appendJsonValue(value: Any?): StringBuilder =
            when (value) {
                null -> append("null")
                is String -> append(Json.encode(value))
                is Number, is Boolean -> append(value.toString())
                is JsonObject -> append(value.encode())
                is JsonArray -> append(value.encode())
                else -> throw IllegalArgumentException("Unsupported JSON value type: ${value::class}")
            }

    }

    /**
     * Write the current buffer to the underlying destination if it is appropriate (e.g. exceeds minimum size requirements), or if it is forced.
     *
     * NOTE: The [sb] buffer won't be reset by the base class, so this should be done in this function if required for the implementation.
     *
     * @param sb The buffer to write if required.
     * @param force Flag to indicate that writing should occur without performing any other checks.
     */
    protected abstract fun checkWrite(sb: StringBuilder, force: Boolean = false)

    /**
     * Notification the response has been completed.
     *
     * This should handle the reaming buffer, then reset it if the response can be reused.
     *
     * @param sb The remaining buffer when the response was completed.
     */
    protected abstract fun onResponseCompleted(sb: StringBuilder)

}
