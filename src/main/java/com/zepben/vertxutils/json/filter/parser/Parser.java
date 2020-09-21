/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json.filter.parser;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.json.filter.FilterException;

import static com.zepben.vertxutils.json.filter.parser.FilterType.EXCLUDE;
import static com.zepben.vertxutils.json.filter.parser.FilterType.INCLUDE;
import static com.zepben.vertxutils.json.filter.parser.Token.*;

@EverythingIsNonnullByDefault
public class Parser {

    public static Node parse(String specification)
        throws FilterException {
        Node root = new Node();
        Lexer lexer = new Lexer(specification);

        lexer.nextToken(IDENTIFIER, DASH);
        if (lexer.currentToken() == DASH) {
            root.setFilterType(EXCLUDE);
            lexer.nextToken(IDENTIFIER);
        } else {
            root.setFilterType(INCLUDE);
        }

        parseNode(root, lexer, END);

        return root;
    }

    private static void parseNode(Node node, Lexer lexer, Token endingToken) throws FilterException {
        Node root = node;
        while (lexer.currentToken() != endingToken) {
            node = node.addOrGetChild(lexer.currentContent());
            lexer.nextToken(OPEN, COMMA, DOT, endingToken);
            if (lexer.currentToken() == DOT) {
                lexer.nextToken(IDENTIFIER);
            }
            if (lexer.currentToken() == OPEN) {
                lexer.nextToken(IDENTIFIER);
                parseNode(node, lexer, CLOSE);
                lexer.nextToken(COMMA, endingToken);
            }
            if (lexer.currentToken() == COMMA) {
                lexer.nextToken(IDENTIFIER);
                parseNode(root, lexer, endingToken);
            }
        }
    }

}
