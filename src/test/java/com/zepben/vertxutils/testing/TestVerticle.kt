/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.testing

import com.zepben.testutils.junit.SystemLogExtension
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import org.junit.jupiter.api.extension.RegisterExtension

class TestVerticle : AbstractVerticle() {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

        var onStart: (Promise<Void>) -> Unit = { it.complete() }
        var onStop: (Promise<Void>) -> Unit = { it.complete() }

    }

    override fun start(startPromise: Promise<Void>) {
        onStart(startPromise)
    }

    override fun stop(stopPromise: Promise<Void>) {
        onStop(stopPromise)
    }

}
