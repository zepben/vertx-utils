/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.chunked;

import kotlin.Unit;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CaptureChunkedJsonResponseTest {

    @Test
    public void captured() {
        CaptureChunkedJsonResponse response = new CaptureChunkedJsonResponse();

        response.ofArray(jsonArray -> {
            jsonArray.item("this is");
            assertThat(response.toString(), equalTo("[\"this is\""));

            jsonArray.item("my");
            assertThat(response.toString(), equalTo("[\"this is\",\"my\""));

            jsonArray.item("test data");
            assertThat(response.toString(), equalTo("[\"this is\",\"my\",\"test data\""));

            return Unit.INSTANCE;
        });

        assertThat(response.toString(), equalTo("[\"this is\",\"my\",\"test data\"]"));
    }

}
