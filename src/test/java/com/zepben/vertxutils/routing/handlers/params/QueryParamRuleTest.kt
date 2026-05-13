/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.routing.handlers.params.RequestValueConverter.Companion.create
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class QueryParamRuleTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val converter = create("string") { s: String -> s }

    @Test
    fun basicConstructor() {
        val rule = QueryParamRule.of("test", converter)
        assertThat(rule.name, equalTo("test"))
        assertThat(rule.converter, equalTo(converter))
        assertThat(rule.isRequired, equalTo(false))
        assertThat(rule.defaultValue, nullValue())
    }

    @Test
    fun defaultValueConstructor() {
        val rule = QueryParamRule.of("test", converter, "default")
        assertThat(rule.name, equalTo("test"))
        assertThat(rule.converter, equalTo(converter))
        assertThat(rule.isRequired, equalTo(false))
        assertThat(rule.defaultValue, equalTo("default"))
    }

    @Test
    fun ofRequiredConstructor() {
        val rule = QueryParamRule.ofRequired("test", converter)
        assertThat(rule.name, equalTo("test"))
        assertThat(rule.converter, equalTo(converter))
        assertThat(rule.isRequired, equalTo(true))
        assertThat(rule.defaultValue, nullValue())
    }

}
