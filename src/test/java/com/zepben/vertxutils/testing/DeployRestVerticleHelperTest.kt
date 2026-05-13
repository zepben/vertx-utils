/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.testing

import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class DeployRestVerticleHelperTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @AfterEach
    fun afterEach() {
        TestVerticle.onStart = { it.complete() }
        TestVerticle.onStop = { it.complete() }
    }

    @Test
    fun coverageOnlyTest() {
        val helper = buildHelper()
        helper.requestSpec
        helper.randomPortNumber
        helper.close()

        TestVerticle.onStart = { it.fail("test start fail") }

        expect { buildHelper() }.toThrow<AssertionError>().withMessage("test start fail")
    }

    private fun buildHelper(): DeployRestVerticleHelper =
        DeployRestVerticleHelper(TestVerticle::class.java, JsonObject())

}
