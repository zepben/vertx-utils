/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter

import com.zepben.vertxutils.json.filter.parser.Node
import com.zepben.vertxutils.json.filter.parser.Parser.parse

/**
 * **FilterSpecification**
 * 
 * Specifies json fields to filter. Can be used to specify fields to exclude or to specify fields to include (i.e., exclude everything but).
 * 
 * For example: a.b.c or a.b.c,x.y.x or a(b,c(d)) or -a.b.c or -a.b.c,-x.y.x or -a(b,c(d))
 */
class FilterSpecification(
    val root: Node = Node(),
) {

    constructor(filter: String) : this(root = parse(filter))

    val filter: String
        get() = root.toString()

    override fun toString(): String = root.toString()

    fun getSubfilter(location: String): FilterSpecification? =
        root.getDescendent(location)?.let { FilterSpecification(it) }

}
