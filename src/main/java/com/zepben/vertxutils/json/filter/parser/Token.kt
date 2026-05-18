/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter.parser

import java.util.regex.Pattern

/**
 * The tokens that the filter specification can be tokenised into.
 */
enum class Token(pattern: String) {

    IDENTIFIER("[a-zA-Z][a-zA-Z0-9]*"),
    OPEN("\\("),
    CLOSE("\\)"),
    COMMA(","),
    DOT("\\."),
    END("$"),
    DASH("-"),
    NONE("");

    val compiledPattern: Pattern = Pattern.compile("^$pattern")

}
