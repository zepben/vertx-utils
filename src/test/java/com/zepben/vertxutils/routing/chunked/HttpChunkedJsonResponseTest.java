/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.chunked;

import io.vertx.core.http.HttpServerResponse;
import kotlin.Unit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class HttpChunkedJsonResponseTest {

    @Mock
    private HttpServerResponse httpServerResponse;
    private AutoCloseable mockitoSession;

    @BeforeEach
    public void setUp() {
        mockitoSession = openMocks(this);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockitoSession.close();
    }

    @Test
    public void doEnd() {
        doReturn(null).when(httpServerResponse).end(anyString());

        // Check that ending the response sends the remaining buffer.
        new HttpChunkedJsonResponse(httpServerResponse, ChunkedJsonResponse.DEFAULT_BUFFER_SIZE).ofArray(array -> Unit.INSTANCE);
        verify(httpServerResponse).end("[]");
    }

    @Test
    public void doCheckWrite() {
        doReturn(null).when(httpServerResponse).write(anyString());

        // NOTE: Buffer needs to be big enough for the added escaping.
        new HttpChunkedJsonResponse(httpServerResponse, 14).ofArray(
            jsonArray -> {
                //
                // NOTE: Every add of an item does a non-forced write check.
                //

                // Check that a non-forced send does nothing if the buffer is undersized.
                jsonArray.item("this");
                jsonArray.item("is");
                verify(httpServerResponse, never()).write(anyString());

                // Check that a forced send works even if the buffer is undersized.
                jsonArray.checkWrite(true);
                verify(httpServerResponse).write("[\"this\",\"is\"");

                // Check that the buffer has been reset and is again undersized.
                clearInvocations(httpServerResponse);
                jsonArray.item("my");
                verify(httpServerResponse, never()).write(anyString());

                // Check that a non-forced sends works once the buffer size is exceeded.
                jsonArray.item("test data");
                verify(httpServerResponse).write(",\"my\",\"test data\"");

                return Unit.INSTANCE;
            }
        );
    }

    @Test
    public void responseCheck() {
        doReturn(true).when(httpServerResponse).closed();
        new HttpChunkedJsonResponse(httpServerResponse, ChunkedJsonResponse.DEFAULT_BUFFER_SIZE).ofArray(array -> Unit.INSTANCE);
        verify(httpServerResponse, never()).end(anyString());

        doReturn(false).when(httpServerResponse).closed();
        new HttpChunkedJsonResponse(httpServerResponse, ChunkedJsonResponse.DEFAULT_BUFFER_SIZE).ofArray(array -> Unit.INSTANCE);
        verify(httpServerResponse).end("[]");

        // Mark the response as closed to ensure the buffer isn't cleared when sending.
        doReturn(true).when(httpServerResponse).closed();
        doReturn(null).when(httpServerResponse).write(anyString());
        new HttpChunkedJsonResponse(httpServerResponse, 10).ofArray(jsonArray -> {
            jsonArray.item("this");
            jsonArray.item("is");
            jsonArray.checkWrite(true);
            jsonArray.item("my");
            jsonArray.item("test data");

            verify(httpServerResponse, never()).write(anyString());

            // Mark the response as open to ensure the buffer is sent on array close.
            doReturn(false).when(httpServerResponse).closed();

            return Unit.INSTANCE;
        });

        verify(httpServerResponse).end("[\"this\",\"is\",\"my\",\"test data\"]");
    }

}
