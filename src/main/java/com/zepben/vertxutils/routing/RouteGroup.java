/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.annotations.EverythingIsNonnullByDefault;

import java.util.List;

@EverythingIsNonnullByDefault
public interface RouteGroup {

    String mountPath();

    List<Route> routes();

    static RouteGroup create(String mountPath, List<Route> routes) {
        return new RouteGroup() {
            @Override
            public String mountPath() {
                return mountPath;
            }

            @Override
            public List<Route> routes() {
                return routes;
            }
        };
    }
}
