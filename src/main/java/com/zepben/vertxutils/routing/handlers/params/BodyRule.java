/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.buffer.Buffer;

@EverythingIsNonnullByDefault
public class BodyRule<T> extends ParamRule<Buffer, T> {

    private final boolean isRequired;

    public BodyRule(RequestValueConverter<Buffer, T> converter, boolean isRequired) {
        super("body", converter);
        this.isRequired = isRequired;
    }

    public boolean isRequired() {
        return isRequired;
    }
}
