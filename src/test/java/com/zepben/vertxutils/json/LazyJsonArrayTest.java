/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json;

import io.vertx.core.json.JsonArray;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class LazyJsonArrayTest {

    @Test
    public void smokeTest() {
        LazyJsonArray lja = new LazyJsonArray(() -> {
            JsonArray jsonArray = new JsonArray();
            IntStream.range(0, 100).forEach(jsonArray::add);
            return jsonArray;
        });

        assertThat(lja.size(), equalTo(100));
        assertThat(lja.getInteger(99), equalTo(99));

    }

}
