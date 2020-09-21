/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;

/**
 * A utility class to help with formatting error messages so they can be consistent across routes.
 */
@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class ErrorFormatter {

    /**
     * Helper method for {@link ErrorFormatter#asJson(List)} that can be called with a single error string.
     *
     * @param error The error message
     * @return The JSON string
     */
    public static String asJson(String error) {
        return asJson(Collections.singletonList(error));
    }

    /**
     * Method takes a list of strings and puts in in a JSON object.
     * This allows route handlers to return errors in a consistent fashion.
     * The JSON object is constructed as follows: {@code {"errors": ["msg1", "msg2", ...]}}
     *
     * @param errors The errors to be included
     * @return The string representation of the JSON object.
     */
    public static String asJson(List<String> errors) {
        return new JsonObject().put("errors", errors).encode();
    }


}
