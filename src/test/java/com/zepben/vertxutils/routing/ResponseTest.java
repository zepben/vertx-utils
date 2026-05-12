/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ResponseTest {

    @Test
    public void statusConstructor() {
        Response response = new Response(HttpResponseStatus.OK, Buffer.buffer(), emptyMap());
        assertThat(response.getStatus(), is(HttpResponseStatus.OK));
        assertThat(response.getBody(), is(Buffer.buffer()));
    }

    @Test
    public void statusBodyConstructor() {
        Buffer buffer = Buffer.buffer("test");
        Response response = new Response(HttpResponseStatus.OK, buffer, emptyMap());
        assertThat(response.getStatus(), is(HttpResponseStatus.OK));
        assertThat(response.getBody(), is(buffer));
    }

    @Test
    public void ofJson() {
        Response response = Response.Companion.ofJson(HttpResponseStatus.OK, "json");
        assertThat(response.getStatus(), is(HttpResponseStatus.OK));
        assertThat(response.getBody(), is(Buffer.buffer("json")));
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), is(MediaType.JSON_UTF_8.toString()));
    }

    @Test
    public void ofText() {
        Response response = Response.Companion.ofText(HttpResponseStatus.OK, "text");
        assertThat(response.getStatus(), is(HttpResponseStatus.OK));
        assertThat(response.getBody(), is(Buffer.buffer("text")));
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), is(MediaType.PLAIN_TEXT_UTF_8.toString()));
    }

}
