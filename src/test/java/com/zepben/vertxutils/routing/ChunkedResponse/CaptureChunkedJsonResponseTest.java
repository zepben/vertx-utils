/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.ChunkedResponse;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class CaptureChunkedJsonResponseTest {

    @Test
    public void captured() {
        CaptureChunkedJsonResponse response = new CaptureChunkedJsonResponse();

        ChunkedJsonResponse.JsonArray jsonArray = response.ofArray();
        jsonArray.addArrayItem("this is").send(false);

        assertThat(response.toString(), equalTo("[this is"));

        jsonArray.addArrayItem("my").send(false);

        assertThat(response.toString(), equalTo("[this is,my"));

        jsonArray.addArrayItem("test data").endArray();

        assertThat(response.toString(), equalTo("[this is,my,test data]"));
    }

}
