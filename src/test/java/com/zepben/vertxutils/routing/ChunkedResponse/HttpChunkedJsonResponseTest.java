/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.ChunkedResponse;

import io.vertx.core.http.HttpServerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class HttpChunkedJsonResponseTest {

    @Mock
    private HttpServerResponse httpServerResponse;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void doEnd() {
        doNothing().when(httpServerResponse).end(any(String.class));

        // Check that ending the response sends the remaining buffer.
        new HttpChunkedJsonResponse(httpServerResponse).ofArray().endArray();
        Mockito.verify(httpServerResponse).end("[]");
    }

    @Test
    public void doSend() {
        doReturn(httpServerResponse).when(httpServerResponse).write(any(String.class));

        ChunkedJsonResponse.JsonArray jsonArray = new HttpChunkedJsonResponse(httpServerResponse, 10).ofArray();

        // Check that a non forced send does nothing if the buffer is under sized.
        jsonArray.addArrayItem("this").send(false);
        Mockito.verify(httpServerResponse, never()).write("[this");

        // Check that a forced send works even if the buffer is under sized.
        jsonArray.addArrayItem("is").send(true);
        Mockito.verify(httpServerResponse).write("[this,is");

        // Check that the buffer has been reset and is again under sized.
        jsonArray.addArrayItem("my").send(false);
        Mockito.verify(httpServerResponse, never()).write(",my");

        // Check that a non forced sends works once the buffer size is exceeded.
        jsonArray.addArrayItem("test data").send(false);
        Mockito.verify(httpServerResponse).write(",my,test data");
    }

    @Test
    public void responseCheck() {
        doReturn(true).when(httpServerResponse).closed();
        new HttpChunkedJsonResponse(httpServerResponse).ofArray().endArray();
        Mockito.verify(httpServerResponse, never()).end(any(String.class));

        doReturn(false).when(httpServerResponse).closed();
        new HttpChunkedJsonResponse(httpServerResponse).ofArray().endArray();
        Mockito.verify(httpServerResponse).end("[]");

        doReturn(true).when(httpServerResponse).closed();
        doReturn(httpServerResponse).when(httpServerResponse).write(any(String.class));
        ChunkedJsonResponse.JsonArray jsonArray = new HttpChunkedJsonResponse(httpServerResponse, 10).ofArray();

        jsonArray.addArrayItem("this").send(false);
        Mockito.verify(httpServerResponse, never()).write(any(String.class));

        jsonArray.addArrayItem("is").send(true);
        Mockito.verify(httpServerResponse, never()).write(any(String.class));

        jsonArray.addArrayItem("my").send(false);
        Mockito.verify(httpServerResponse, never()).write(any(String.class));

        jsonArray.addArrayItem("test data").send(false);
        Mockito.verify(httpServerResponse, never()).write(any(String.class));

        doReturn(false).when(httpServerResponse).closed();
        jsonArray.endArray().send(true);
        Mockito.verify(httpServerResponse).write("[this,is,my,test data]");
    }

}
