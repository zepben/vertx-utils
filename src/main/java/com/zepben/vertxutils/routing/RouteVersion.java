/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.google.errorprone.annotations.Immutable;
import com.zepben.annotations.EverythingIsNonnullByDefault;

@EverythingIsNonnullByDefault
@Immutable
@SuppressWarnings("WeakerAccess")
public class RouteVersion {

    private final int first;
    private final int last;

    public static RouteVersion since(int first) {
        return new RouteVersion(first, Integer.MAX_VALUE);
    }

    public static RouteVersion between(int first, int last) {
        return new RouteVersion(first, last);
    }

    public boolean includes(int version) {
        return (first <= version) && (last >= version);
    }

    private RouteVersion(int first, int last) {
        this.first = first;
        this.last = last;
    }

}
