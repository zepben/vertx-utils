/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter.parser

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.json.filter.FilterException
import com.zepben.vertxutils.json.filter.parser.Parser.parse
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ParserTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun exceptions() {
        expect { parse("") }
            .toThrow<FilterException>()
            .withMessage("Error parsing []. After [] expected one of [IDENTIFIER,DASH] but found []")

        expect { parse("asd(") }
            .toThrow<FilterException>()
            .withMessage("Error parsing [asd(]. After [asd(] expected one of [IDENTIFIER] but found []")
    }

    @Test
    fun simple1() {
        val node = parse("feeders")
        assertThat(node.toString(), equalTo("feeders"))
        assertThat(node.countAllNodes(), equalTo(2))
    }

    @Test
    fun simple2() {
        val node = parse("a.b")
        assertThat(node.toString(), equalTo("a.b"))
        assertThat(node.countAllNodes(), equalTo(3))
    }

    @Test
    fun exclude() {
        val node = parse("-a.b")
        assertThat(node.toString(), equalTo("-a.b"))
        assertThat(node.countAllNodes(), equalTo(3))
    }

    @Test
    fun medium1() {
        val node = parse("feeders(assets,feeder)")
        assertThat(node.toString(), equalTo("feeders(assets,feeder)"))
        assertThat(node.countAllNodes(), equalTo(4))
    }

    @Test
    fun medium2() {
        val node = parse("a(b,c),a.d")
        assertThat(node.toString(), equalTo("a(b,c,d)"))
        assertThat(node.countAllNodes(), equalTo(5))
    }

    @Test
    fun complex() {
        val node = parse("feeders(assets(id,isOpen,lngLat,name,symbol),feeder(id,name,state))")
        assertThat(node.toString(), equalTo("feeders(assets(id,isOpen,lngLat,name,symbol),feeder(id,name,state))"))
        assertThat(node.countAllNodes(), equalTo(12))
    }

}
