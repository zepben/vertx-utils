/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import org.slf4j.Logger;

import java.util.function.BiConsumer;

@SuppressWarnings("WeakerAccess")
public class RouteRegisterLogger implements BiConsumer<String, Route> {

    private final Logger logger;

    public RouteRegisterLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void accept(String path, Route route) {
        route.methods().forEach(method -> logger.info(method + ": " + path));
    }

}
