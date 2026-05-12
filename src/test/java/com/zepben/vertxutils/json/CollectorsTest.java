/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json;

import io.vertx.core.json.JsonArray;
import kotlin.sequences.SequencesKt;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CollectorsTest {

    @Test
    public void toJsonArray() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        JsonArray jsonArray = Collectors.INSTANCE.toJsonArray(SequencesKt.asSequence(list.stream().iterator()));

        assertThat(list.size(), equalTo(jsonArray.size()));
        for (int i = 0; i < list.size(); i++)
            assertThat(list.get(i), equalTo(jsonArray.getInteger(i)));
    }

}
