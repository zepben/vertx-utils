/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class QueryParamsTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    private val p1 = QueryParamRule.of("p1", ParamType.STRING)
    private val p2 = QueryParamRule.of("p2", ParamType.STRING, "default2")
    private val p3 = QueryParamRule.of("p3", ParamType.STRING)
    private val p4 = QueryParamRule.of("p4", ParamType.STRING, "default4")
    private val notRegistered = QueryParamRule.of("none", ParamType.STRING)

    private val params = QueryParams(
        setOf(p1, p2, p3, p4),
        mapOf(
            p1.name to listOf("aString", "4"),
            p2.name to listOf("value"),
        ),
    )

    @Test
    fun get() {
        assertThat(params[p1], equalTo("aString"))
        assertThat(params[p2], equalTo("value"))
        expect { params[p3] }.toThrow<IllegalArgumentException>()
        assertThat(params[p4], equalTo(p4.defaultValue))
        expect { params[notRegistered] }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun getOrElse() {
        assertThat(params.getOrElse(p1, "other1"), equalTo(params[p1]))
        assertThat(params.getOrElse(p2, "other2"), equalTo(params[p2]))
        assertThat(params.getOrElse(p3, "other3"), equalTo("other3"))
        assertThat(params.getOrElse(p3, "other4"), equalTo("other4"))
        assertThat(params.getOrElse(p3, null), nullValue())
        expect { params.getOrElse(notRegistered, "other") }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun getAll() {
        assertThat(params.getAll(p1), contains("aString", "4"))
        assertThat(params.getAll(p2), contains("value"))
        expect { params.getAll(p3) }.toThrow<IllegalArgumentException>()
        assertThat(params.getAll(p4), contains(p4.defaultValue))
        expect { params.getAll(notRegistered) }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun getAllOrElse() {
        assertThat(
            params.getAllOrElse(p1, "other1"),
            contains<Any?>(*params.getAll(p1).toTypedArray()),
        )
        assertThat(
            params.getAllOrElse(p2, "other2"),
            contains<Any?>(*params.getAll(p2).toTypedArray()),
        )
        assertThat(params.getAllOrElse(p3, "other3"), contains("other3"))
        assertThat(params.getAllOrElse(p4, "other4"), contains("other4"))
        expect { params.getAllOrElse(notRegistered, "other4") }.toThrow<IllegalArgumentException>()

        assertThat(
            params.getAllOrElse(p1, listOf("other1", "other2")),
            contains<Any?>(*params.getAll(p1).toTypedArray()),
        )
        assertThat(
            params.getAllOrElse(p2, listOf("other3", "other4")),
            contains<Any?>(*params.getAll(p2).toTypedArray()),
        )
        assertThat(
            params.getAllOrElse(p3, listOf("other5", "other6")),
            contains("other5", "other6"),
        )
        assertThat(
            params.getAllOrElse(p3, listOf("other7", "other8")),
            contains("other7", "other8"),
        )
        expect { params.getAllOrElse(notRegistered, listOf("other7", "other8")) }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun exists() {
        assertThat(params.contains(p1), equalTo(true))
        assertThat(params.contains(p2), equalTo(true))
        assertThat(params.contains(p3), equalTo(false))
        assertThat(params.contains(p4), equalTo(false))
        assertThat(params.contains(notRegistered), equalTo(false))
    }

    @Test
    internal fun `has non-nullable default or throws when using get`() {
        //
        // NOTE: This is here to make sure we return non-null query parameters, as this is not detected with passing stuff
        //       directly to hamcrest.
        fun validate(value: String, expected: String) =
            assertThat(value, equalTo(expected))

        validate(params[p4], p4.defaultValue!!)

        expect { params[p3] }.toThrow<IllegalArgumentException>().withMessage(
            "INTERNAL ERROR: Param ${p3.name} has no values and no default. Either mark the param as required, provide a default or use with getOrElse or getAllOrElse.",
        )
    }

}
