/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter.parser

import com.zepben.vertxutils.json.filter.FilterException

/**
 * @param specification The string to be tokenised.
 */
internal class Lexer(
    private val specification: String,
) {

    /**
     * If a token has matched, this is the content of the token.
     */
    var currentContent: String? = null
        private set

    /**
     * If a token has matched, this is the token, otherwise NONE
     */
    var currentToken = Token.NONE
        private set

    /**
     * Where in the string the tokenising is up to
     */
    private var currentPosition = 0

    @Throws(FilterException::class)
    fun nextToken(vararg lookingFor: Token) {
        skipWhitespace()

        lookingFor.forEach { t ->
            val m = t.compiledPattern
                .matcher(specification)
                .region(currentPosition, specification.length)
            if (m.find()) {
                currentContent = specification.substring(currentPosition, m.end())
                currentPosition = m.end()
                currentToken = t
                return
            }
        }
        throw FilterException(specification, currentPosition, *lookingFor)
    }

    private fun skipWhitespace() {
        while ((currentPosition < specification.length) && Character.isWhitespace(specification[currentPosition]))
            currentPosition++
    }

}
