/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.zepben.annotations.EverythingIsNonnullByDefault;

@EverythingIsNonnullByDefault
@SuppressWarnings("WeakerAccess")
public class StaticAssetsRouteConfig {

    private final String webRoot;
    private final boolean isCaching;

    public static StaticAssetsRouteConfig of(String webRoot, boolean isCaching) {
        return new StaticAssetsRouteConfig(webRoot, isCaching);
    }

    public String webRoot() {
        return webRoot;
    }

    public boolean isCaching() {
        return isCaching;
    }

    private StaticAssetsRouteConfig(String webRoot, boolean isCaching) {
        this.webRoot = webRoot;
        this.isCaching = isCaching;
    }

}
