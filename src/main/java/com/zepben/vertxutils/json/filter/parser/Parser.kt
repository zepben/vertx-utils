/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter.parser

import com.zepben.vertxutils.json.filter.FilterException

object Parser {

    @Throws(FilterException::class)
    fun parse(specification: String): Node {
        val lexer = Lexer(specification)

        lexer.nextToken(Token.IDENTIFIER, Token.DASH)
        val root = Node(
            filterType = if (lexer.currentToken == Token.DASH) {
                lexer.nextToken(Token.IDENTIFIER)
                FilterType.EXCLUDE
            } else {
                FilterType.INCLUDE
            },
        )

        parseNode(root, lexer, Token.END)

        return root
    }

    @Throws(FilterException::class)
    private fun parseNode(node: Node, lexer: Lexer, endingToken: Token) {
        var node = node
        val root = node
        while (lexer.currentToken != endingToken) {
            node = node.addOrGetChild(lexer.currentContent!!)
            lexer.nextToken(Token.OPEN, Token.COMMA, Token.DOT, endingToken)
            if (lexer.currentToken == Token.DOT) {
                lexer.nextToken(Token.IDENTIFIER)
            }
            if (lexer.currentToken == Token.OPEN) {
                lexer.nextToken(Token.IDENTIFIER)
                parseNode(node, lexer, Token.CLOSE)
                lexer.nextToken(Token.COMMA, endingToken)
            }
            if (lexer.currentToken == Token.COMMA) {
                lexer.nextToken(Token.IDENTIFIER)
                parseNode(root, lexer, endingToken)
            }
        }
    }

}
