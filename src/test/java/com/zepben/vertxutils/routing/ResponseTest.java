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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("UnstableApiUsage")
public class ResponseTest {

    @Test
    public void statusConstructor() {
        Response response = new Response(HttpResponseStatus.OK);
        assertThat(response.status(), is(HttpResponseStatus.OK));
        assertThat(response.body(), is(Buffer.buffer()));
    }

    @Test
    public void statusBodyConstructor() {
        Buffer buffer = Buffer.buffer("test");
        Response response = new Response(HttpResponseStatus.OK, buffer);
        assertThat(response.status(), is(HttpResponseStatus.OK));
        assertThat(response.body(), is(buffer));
    }

    @Test
    public void setters() {
        Buffer buffer = Buffer.buffer("test");
        Response response = new Response(HttpResponseStatus.OK);
        response.setStatus(HttpResponseStatus.BAD_REQUEST)
            .setBody(buffer);

        assertThat(response.status(), is(HttpResponseStatus.BAD_REQUEST));
        assertThat(response.body(), is(buffer));
    }

    @Test
    public void ofJson() {
        Response response = Response.ofJson(HttpResponseStatus.OK, "json");
        assertThat(response.status(), is(HttpResponseStatus.OK));
        assertThat(response.body(), is(Buffer.buffer("json")));
        assertThat(response.headers().get(HttpHeaders.CONTENT_TYPE), is(MediaType.JSON_UTF_8.toString()));
    }

    @Test
    public void ofText() {
        Response response = Response.ofText(HttpResponseStatus.OK, "text");
        assertThat(response.status(), is(HttpResponseStatus.OK));
        assertThat(response.body(), is(Buffer.buffer("text")));
        assertThat(response.headers().get(HttpHeaders.CONTENT_TYPE), is(MediaType.PLAIN_TEXT_UTF_8.toString()));
    }

}
