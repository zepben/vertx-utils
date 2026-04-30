/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.chunked

import io.vertx.core.json.Json

abstract class ChunkedJsonResponse(
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
) {

    companion object {

        const val DEFAULT_BUFFER_SIZE = 1 shl 21
    }

    private val sb = StringBuilder(bufferSize)
    private val needsSeparatorStack = ArrayDeque<Boolean>()

    // --- Public DSL entry points ---

    fun jsonObject(block: JsonObjectBuilder.() -> Unit) {
        check(sb.isEmpty()) { "Can't reuse a non-clean response builder" }
        JsonObjectBuilder().build(block)
    }

    fun jsonArray(block: JsonArrayBuilder.() -> Unit) {
        check(sb.isEmpty()) { "Can't reuse a non-clean response builder" }
        JsonArrayBuilder().build(block)
    }

    // --- Builders ---

    inner class JsonObjectBuilder : JsonBuilder<JsonObjectBuilder>("{", "}") {

        fun field(key: String, value: Any?) {
            sb.writeKey(key).appendJsonValue(value)
            checkWrite(sb)
        }

        fun obj(key: String, block: JsonObjectBuilder.() -> Unit) {
            sb.writeKey(key)
            JsonObjectBuilder().build(block)
        }

        fun array(key: String, block: JsonArrayBuilder.() -> Unit) {
            sb.writeKey(key)
            JsonArrayBuilder().build(block)
        }

        private fun StringBuilder.writeKey(key: String): StringBuilder =
            maybeAppendSeparator()
                .append(Json.encode(key))
                .append(":")

    }

    inner class JsonArrayBuilder : JsonBuilder<JsonArrayBuilder>("[", "]") {

        fun item(value: Any?) {
            sb.maybeAppendSeparator().appendJsonValue(value)
            checkWrite(sb)
        }

        fun obj(block: JsonObjectBuilder.() -> Unit) {
            JsonObjectBuilder().build(block)
        }

        fun array(block: JsonArrayBuilder.() -> Unit) {
            JsonArrayBuilder().build(block)
        }
    }

    abstract inner class JsonBuilder<B : JsonBuilder<B>>(
        private val openToken: String,
        private val closeToken: String,
    ) {

        fun build(block: B.() -> Unit) {
            begin()
            @Suppress("UNCHECKED_CAST")
            (this as B).block()
            end()
        }

        protected fun begin() {
            sb.maybeAppendSeparator().append(openToken)
            needsSeparatorStack.addLast(false)
        }

        protected fun end() {
            sb.append(closeToken)
            needsSeparatorStack.removeLast()

            if (needsSeparatorStack.isEmpty())
                end(sb)
            else
                checkWrite(sb)
        }

        protected fun StringBuilder.maybeAppendSeparator(): StringBuilder {
            if (needsSeparatorStack.isNotEmpty()) {
                val needsSeparator = needsSeparatorStack.removeLast()
                if (needsSeparator)
                    append(",")
                needsSeparatorStack.addLast(true)
            }
            return this
        }

        protected fun StringBuilder.appendJsonValue(value: Any?): StringBuilder =
            when (value) {
                null -> append("null")
                is String -> append(Json.encode(value))
                is Number, is Boolean -> append(value.toString())
                else -> error("Unsupported JSON value type: ${value::class}")
            }

    }

    protected abstract fun checkWrite(sb: StringBuilder, force: Boolean = false)

    protected abstract fun end(sb: StringBuilder)

}
