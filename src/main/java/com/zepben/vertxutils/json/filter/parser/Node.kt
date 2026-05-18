/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter.parser

import java.util.*

/**
 * A Node in the filter tree.
 *
 * @property content The string content of this node if there is any.
 * @property filterType Is this an exclude or include node?
 */
data class Node(
    val content: String? = null,
    val filterType: FilterType = FilterType.PASSTHROUGH,
) {

    // The children of this Node (might be empty)
    private val children = TreeMap<String, Node>()

    fun getChildren(): Collection<Node> = children.values

    fun getChild(name: String): Node? = children[name]

    fun getDescendent(name: String): Node? {
        var descendant: Node? = this
        name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().forEach { n ->
            descendant ?: return null
            descendant = descendant.getChild(n)
        }
        return descendant
    }

    fun addOrGetChild(content: String): Node =
        children.getOrPut(content) { Node(content, filterType) }

    fun countAllNodes(): Int =
        children.values.sumOf { it.countAllNodes() } + 1

    override fun toString(): String {
        val childrenString = children.values.joinToString(separator = ",") { it.toString().replace("-", "") }
        return when {
            content == null -> "${filterType.denotedBy}$childrenString"
            children.isEmpty() -> content
            children.size == 1 -> "$content.$childrenString"
            else -> "$content($childrenString)"
        }
    }

}
