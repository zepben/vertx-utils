/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter

import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class FilterSpecificationTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun parsesCorrectly() {
        val filter = "key1(key11.key111,key12.key121)"
        validateFilter(filter, filter)

        validateFilter("key1.key11,key1.key12", "key1(key11,key12)")
    }

    private fun validateFilter(filter: String, expected: String?) {
        val filterSpecification = FilterSpecification(filter)
        assertThat(filterSpecification.toString(), equalTo(expected))
    }

}
