/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter

import com.zepben.vertxutils.json.filter.parser.FilterType
import com.zepben.vertxutils.json.filter.parser.Node
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class JsonObjectFilter {

    /**
     * Applies a filter to a JsonObject. Note that this mutates the given object and returns it.
     */
    fun apply(json: JsonObject, fs: FilterSpecification): JsonObject {
        apply(fs.root, json)
        return json
    }

    private fun apply(node: Node, obj: Any) {
        when (node.filterType) {
            FilterType.INCLUDE -> includeSpecified(node, obj)
            FilterType.EXCLUDE -> excludeSpecified(node, obj)
            FilterType.PASSTHROUGH -> {}
        }
    }

    private fun includeSpecified(node: Node, obj: Any) {
        if (node.getChildren().isEmpty())
            return  // Nothing to do

        when (obj) {
            is JsonArray -> obj.forEach { value -> apply(node, value) }

            is JsonObject -> {
                val fieldsToInclude = node
                    .getChildren()
                    .asSequence()
                    .mapNotNull(Node::content)
                    .toSet()

                val fieldsToRemove = obj.fieldNames() - fieldsToInclude
                fieldsToRemove.forEach { obj.remove(it) }

                node.getChildren().forEach { child ->
                    obj.getValue(child.content)?.also {
                        apply(child, it)
                    }
                }
            }
        }
    }

    private fun excludeSpecified(node: Node, obj: Any) {
        when (obj) {
            is JsonArray -> obj.forEach { value -> apply(node, value) }

            is JsonObject -> {
                node.getChildren().forEach { child ->
                    if (child.getChildren().isEmpty()) {
                        obj.remove(child.content)
                    } else {
                        apply(child, obj.getValue(child.content))
                    }
                }
            }
        }
    }

    companion object {

        fun applyFilter(json: JsonObject, fs: FilterSpecification): JsonObject =
            JsonObjectFilter().apply(json, fs)

    }

}
