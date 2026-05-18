/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter

import com.zepben.vertxutils.json.filter.parser.Token

class FilterException(specification: String, from: Int, vararg expected: Token) : Exception(
    "Error parsing [$specification]. After [${specification.substring(0, from)}] expected one of [${
        expected.joinToString(",")
    }] but found [${specification.substring(from)}]",
)
