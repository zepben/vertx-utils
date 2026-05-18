/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

class BadParamException private constructor(msg: String) : RuntimeException(msg) {

    companion object {

        fun missingParam(name: String): BadParamException =
            BadParamException("missing required parameter $name")

        fun invalidParam(rule: ParamRule<*, *>, value: String, reason: String?): BadParamException =
            BadParamException("Parameter '${rule.name}' with value '$value' is invalid. Expected format '${rule.converter.expectedFormat}': $reason")

        fun missingBody(): BadParamException =
            BadParamException("required body is missing")

        fun invalidBody(rule: BodyRule<*>, reason: String?): BadParamException =
            BadParamException("body is invalid. Expected format '${rule.converter.expectedFormat}': $reason")

    }

}
