/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class StaticAssetsRouteConfigTest {

    @Test
    public void accessors() {
        StaticAssetsRouteConfig config = StaticAssetsRouteConfig.of("root1", true);
        assertThat(config.webRoot(), equalTo("root1"));
        assertThat(config.isCaching(), equalTo(true));

        config = StaticAssetsRouteConfig.of("root2", false);
        assertThat(config.webRoot(), equalTo("root2"));
        assertThat(config.isCaching(), equalTo(false));
    }

}
