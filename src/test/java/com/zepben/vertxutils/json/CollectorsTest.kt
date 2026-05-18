/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.json.Collectors.toJsonArray
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CollectorsTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun toJsonArray() {
        val list = listOf(1, 2, 3, 4, 5)
        val jsonArray = list.asSequence().toJsonArray()

        assertThat(list.size, equalTo(jsonArray.size()))
        list.forEachIndexed { index, value -> assertThat(value, equalTo(jsonArray.getInteger(index))) }
    }

}
