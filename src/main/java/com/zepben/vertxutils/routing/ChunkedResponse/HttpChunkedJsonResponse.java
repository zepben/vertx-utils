/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.ChunkedResponse;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.http.HttpServerResponse;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class HttpChunkedJsonResponse extends ChunkedJsonResponse {

    private final HttpServerResponse response;
    private final int bufferSize;

    public HttpChunkedJsonResponse(HttpServerResponse response) {
        this(response, DEFAULT_BUFFER_SIZE);
    }

    public HttpChunkedJsonResponse(HttpServerResponse response, int bufferSize) {
        super(bufferSize);

        this.response = response;
        this.bufferSize = bufferSize;
    }

    @Override
    protected void end(StringBuilder sb) {
        if (!response.closed()){
            response.end(sb.toString());
        }
    }

    @Override
    protected void send(boolean force, StringBuilder sb) {
        if ((force || sb.length() >= bufferSize) && !response.closed()) {
            response.write(sb.toString());
            sb.setLength(0);
        }
    }

}
