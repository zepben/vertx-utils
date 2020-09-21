/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class LazyJsonObjectTest {

    private static final String KEY1 = "pi";
    private static final String KEY2 = "zepben";
    private static final double VALUE1 = Math.PI;
    private static final String VALUE2 = "Zeppelin Bend";

    @Test
    public void lazyObjectTest() {
        LazyJsonObject ljo = new LazyJsonObject(() -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(KEY1, VALUE1);
            jsonObject.put(KEY2, VALUE2);
            return jsonObject;
        });

        String encoded = ljo.encode();
        JsonObject decoded = new JsonObject(encoded);

        assertThat(decoded.getDouble(KEY1), equalTo(VALUE1));
        assertThat(decoded.getString(KEY2), equalTo(VALUE2));
    }

    @Test
    public void lazyIncludedMemberTest() {
        final MutableInt callCount = new MutableInt(0);

        LazyJsonObject jsonObject = new LazyJsonObject();
        jsonObject.put(KEY1, () -> {
            callCount.increment();
            return VALUE1;
        });
        jsonObject.put(KEY2, VALUE2);

        String encoded = jsonObject.encode();
        JsonObject decoded = new JsonObject(encoded);

        assertThat(decoded.getDouble(KEY1), equalTo(VALUE1));
        assertThat(decoded.getString(KEY2), equalTo(VALUE2));
        assertThat(callCount.getValue(), equalTo(1));
    }

    @Test
    public void lazyExcludedMemberTest() {
        final MutableInt callCount = new MutableInt(0);

        LazyJsonObject jsonObject = new LazyJsonObject();
        jsonObject.put(KEY1, () -> {
            callCount.increment();
            return VALUE1;
        });
        jsonObject.put(KEY2, VALUE2);

        jsonObject.remove("pi");

        String encoded = jsonObject.encode();
        JsonObject decoded = new JsonObject(encoded);

        assertThat(decoded.getDouble(KEY1), is(nullValue()));
        assertThat(decoded.getString(KEY2), equalTo(VALUE2));
        assertThat(callCount.getValue(), equalTo(0));
    }

    @Test
    public void lazyObjectWithIncludedLazyMemberTest() {
        final MutableInt callCount = new MutableInt(0);

        LazyJsonObject ljo = new LazyJsonObject(() -> {
            LazyJsonObject jsonObject = new LazyJsonObject();
            jsonObject.put(KEY1, () -> {
                callCount.increment();
                return VALUE1;
            });
            jsonObject.put(KEY2, VALUE2);
            return jsonObject;
        });

        String encoded = ljo.encode();
        JsonObject decoded = new JsonObject(encoded);

        assertThat(decoded.getDouble(KEY1), equalTo(VALUE1));
        assertThat(decoded.getString(KEY2), equalTo(VALUE2));

        assertThat(callCount.getValue(), equalTo(1));
    }

    @Test
    public void lazyObjectWithExcludedLazyMemberTest() {
        final MutableInt callCount = new MutableInt(0);

        LazyJsonObject ljo = new LazyJsonObject(() -> {
            LazyJsonObject jsonObject = new LazyJsonObject();
            jsonObject.put(KEY1, () -> {
                callCount.increment();
                return VALUE1;
            });
            jsonObject.put(KEY2, VALUE2);
            return jsonObject;
        });

        ljo.remove("pi");

        String encoded = ljo.encode();

        JsonObject decoded = new JsonObject(encoded);

        assertThat(decoded.getDouble(KEY1), is(nullValue()));
        assertThat(decoded.getString(KEY2), equalTo(VALUE2));

        assertThat(callCount.getValue(), equalTo(0));
    }

}
