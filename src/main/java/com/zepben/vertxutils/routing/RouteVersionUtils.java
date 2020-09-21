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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EverythingIsNonnullByDefault
@SuppressWarnings("WeakerAccess")
public class RouteVersionUtils {

    public static <T extends VersionableRoute> List<Route> forVersion(T[] availableRoutes, int version, Function<T, Route> routeFactory) {
        return Stream.of(availableRoutes)
            .filter(rv -> rv.routeVersion().includes(version))
            .map(routeFactory)
            .collect(Collectors.toList());
    }

}
