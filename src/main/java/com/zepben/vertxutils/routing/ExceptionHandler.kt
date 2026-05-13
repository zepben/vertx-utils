/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class ExceptionHandler<T : Throwable> internal constructor(
    private val tClass: Class<T>,
    private val handler: (T, RoutingContext?) -> Unit,
) : Handler<RoutingContext?> {

    override fun handle(context: RoutingContext?) {
        // The context shouldn't ever be null in our use case.
        requireNotNull(context)

        if (!tClass.isInstance(context.failure())) {
            context.next()
            return
        }

        handler(tClass.cast(context.failure()), context)
    }

    companion object {

        internal inline fun <reified T : Exception> of(noinline handler: (T, RoutingContext?) -> Unit): ExceptionHandler<T> =
            ExceptionHandler(T::class.java, handler)

    }

}
