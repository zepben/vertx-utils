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

import javax.annotation.Nullable;
import java.util.regex.Matcher;

@EverythingIsNonnullByDefault
class Lexer {

    // The string to be tokenised
    private final String specification;

    // Where in the string the tokenising is up to
    private int currentPosition = 0;

    // If a token has matched, this is the content of the token
    @Nullable
    private String currentContent;

    // If a token has matched, this is the token, otherwise NONE
    private Token currentToken = Token.NONE;

    Lexer(String specification) {
        this.specification = specification;
    }

    void nextToken(Token... lookingFor) throws FilterException {

        skipWhitespace();

        for (Token t : lookingFor) {
            Matcher m = t.compiledPattern
                .matcher(specification)
                .region(currentPosition, specification.length());
            if (m.find()) {
                currentContent = specification.substring(currentPosition, m.end());
                currentPosition = m.end();
                currentToken = t;
                return;
            }
        }
        throw new FilterException(specification, currentPosition, lookingFor);
    }

    @Nullable
    String currentContent() {
        return currentContent;
    }

    @Nullable
    Token currentToken() {
        return currentToken;
    }

    private void skipWhitespace() {
        while (currentPosition < specification.length()
            && Character.isWhitespace(specification.charAt(currentPosition))) {
            currentPosition++;
        }
    }

}
