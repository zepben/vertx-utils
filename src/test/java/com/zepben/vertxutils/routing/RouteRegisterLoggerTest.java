/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import io.vertx.core.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.PUT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

public class RouteRegisterLoggerTest {

    @Test
    public void logsRouteDetails() {
        Logger logger = mock(Logger.class);

        String[] paths = {"/my/path/1", "/my/path/2", "/my/other/3"};
        HttpMethod[][] methods = {{GET}, {PUT}, {GET, PUT}};
        assertThat(paths.length, equalTo(methods.length));

        RouteRegisterLogger routeRegisterLogger = new RouteRegisterLogger(logger);
        for (int i = 0; i < paths.length; ++i) {
            routeRegisterLogger.accept("/mount" + paths[i], Route.builder().path(paths[i]).methods(methods[i]).build());

            for (int j = 0; j < methods[i].length; ++j)
                verify(logger, times(1)).info(methods[i][j] + ": /mount" + paths[i]);
        }

    }

}
