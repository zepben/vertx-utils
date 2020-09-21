/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.ChunkedResponse;

import com.zepben.annotations.EverythingIsNonnullByDefault;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class CaptureChunkedJsonResponse extends ChunkedJsonResponse {

    private String captured = "";

    public CaptureChunkedJsonResponse() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public CaptureChunkedJsonResponse(int bufferSize) {
        super(bufferSize);
    }

    @Override
    protected void end(StringBuilder sb) {
        captured = sb.toString();
    }

    @Override
    protected void send(boolean force, StringBuilder sb) {
        captured = sb.toString();
    }

    @Override
    public String toString() {
        return captured;
    }

}
