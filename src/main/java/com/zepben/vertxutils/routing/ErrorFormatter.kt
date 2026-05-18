/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing

import io.vertx.core.json.JsonObject

/**
 * A utility class to help with formatting error messages so they can be consistent across routes.
 */
object ErrorFormatter {

    /**
     * Helper method for [ErrorFormatter.asJson] that can be called with a single error string.
     * 
     * @param error The error message
     * @return The JSON string
     */
    fun asJson(error: String?): String = asJson(listOf(error))

    /**
     * Method takes a list of strings and puts it in a JSON object.
     * This allows route handlers to return errors in a consistent fashion.
     * The JSON object is constructed as follows: `{"errors": ["msg1", "msg2", ...]}`
     * 
     * @param errors The errors to be included
     * @return The string representation of the JSON object.
     */
    fun asJson(errors: List<String?>): String = JsonObject().put("errors", errors).encode()

}
