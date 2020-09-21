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
import org.junit.jupiter.api.Test;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EverythingIsNonnullByDefault
public class ParserTest {

    @Test
    public void exceptions() {
        expect(() -> Parser.parse(""))
            .toThrow(FilterException.class)
            .withMessage("Error parsing []. After [] expected one of [IDENTIFIER,DASH] but found []");

        expect(() -> Parser.parse("asd("))
            .toThrow(FilterException.class)
            .withMessage("Error parsing [asd(]. After [asd(] expected one of [IDENTIFIER] but found []");
    }

    @Test
    public void simple1() throws FilterException {
        Node node = Parser.parse("feeders");
        assertEquals("feeders", node.toString());
        assertEquals(2, node.countAllNodes());
    }

    @Test
    public void simple2() throws FilterException {
        Node node = Parser.parse("a.b");
        assertEquals("a.b", node.toString());
        assertEquals(3, node.countAllNodes());
    }

    @Test
    public void exclude() throws FilterException {
        Node node = Parser.parse("-a.b");
        assertEquals("-a.b", node.toString());
        assertEquals(3, node.countAllNodes());
    }

    @Test
    public void medium1() throws FilterException {
        Node node = Parser.parse("feeders(assets,feeder)");
        assertEquals("feeders(assets,feeder)", node.toString());
        assertEquals(4, node.countAllNodes());
    }

    @Test
    public void medium2() throws FilterException {
        Node node = Parser.parse("a(b,c),a.d");
        assertEquals("a(b,c,d)", node.toString());
        assertEquals(5, node.countAllNodes());
    }

    @Test
    public void complex() throws FilterException {
        Node node = Parser.parse("feeders(assets(id,isOpen,lngLat,name,symbol),feeder(id,name,state))");
        assertEquals("feeders(assets(id,isOpen,lngLat,name,symbol),feeder(id,name,state))", node.toString());
        assertEquals(12, node.countAllNodes());
    }

}
