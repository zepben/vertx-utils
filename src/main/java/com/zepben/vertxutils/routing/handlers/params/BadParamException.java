/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.zepben.annotations.EverythingIsNonnullByDefault;

@EverythingIsNonnullByDefault
public class BadParamException extends RuntimeException {

    private BadParamException(String msg) {
        super(msg);
    }

    public static BadParamException missingParam(String name) {
        return new BadParamException("missing required parameter " + name);
    }

    public static BadParamException invalidParam(ParamRule<?, ?> rule, String value, String reason) {
        String msg = String.format(
            "Parameter '%s' with value '%s' is invalid. Expected format '%s': %s",
            rule.name(),
            value,
            rule.converter().expectedFormat(),
            reason);

        return new BadParamException(msg);
    }

    public static BadParamException missingBody() {
        return new BadParamException("required body is missing");
    }

    public static BadParamException invalidBody(BodyRule<?> rule, String reason) {
        String msg = String.format(
            "body is invalid. Expected format '%s': %s",
            rule.converter().expectedFormat(),
            reason);

        return new BadParamException(msg);
    }

}
