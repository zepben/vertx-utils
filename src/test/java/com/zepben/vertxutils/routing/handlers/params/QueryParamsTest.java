/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class QueryParamsTest {

    private final QueryParamRule<String> p1 = QueryParamRule.of("p1", ParamType.STRING);
    private final QueryParamRule<String> p2 = QueryParamRule.of("p2", ParamType.STRING, "default2");
    private final QueryParamRule<String> p3 = QueryParamRule.of("p3", ParamType.STRING);
    private final QueryParamRule<String> p4 = QueryParamRule.of("p4", ParamType.STRING, "default4");
    private final QueryParamRule<String> notRegistered = QueryParamRule.of("none", ParamType.STRING);

    private QueryParams params;

    @BeforeEach
    public void setUp() {
        Map<String, List<Object>> paramValues = new HashMap<>();
        paramValues.put(p1.name(), ImmutableList.of("aString", "4"));
        paramValues.put(p2.name(), ImmutableList.of("value"));
        params = new QueryParams(ImmutableSet.of(p1, p2, p3, p4), paramValues);
    }

    @Test
    public void get() {
        assertThat(params.get(p1), is("aString"));
        assertThat(params.get(p2), is("value"));
        expect(() -> params.get(p3)).toThrow(IllegalArgumentException.class);
        assertThat(params.get(p4), is(p4.defaultValue()));
        expect(() -> params.get(notRegistered)).toThrow(IllegalArgumentException.class);
    }

    @Test
    public void getOrElse() {
        assertThat(params.getOrElse(p1, "other1"), is(params.get(p1)));
        assertThat(params.getOrElse(p2, "other2"), is(params.get(p2)));
        assertThat(params.getOrElse(p3, "other3"), is("other3"));
        assertThat(params.getOrElse(p3, "other4"), is("other4"));
        assertThat(params.getOrElse(p3, null), nullValue());
        expect(() -> params.getOrElse(notRegistered, "other")).toThrow(IllegalArgumentException.class);
    }

    @Test
    public void getAll() {
        assertThat(params.getAll(p1), contains("aString", "4"));
        assertThat(params.getAll(p2), contains("value"));
        expect(() -> params.getAll(p3)).toThrow(IllegalArgumentException.class);
        assertThat(params.getAll(p4), contains(p4.defaultValue()));
        expect(() -> params.getAll(notRegistered)).toThrow(IllegalArgumentException.class);
    }

    @Test
    public void getAllOrElse() {
        assertThat(params.getAllOrElse(p1, "other1"), contains(params.getAll(p1).toArray()));
        assertThat(params.getAllOrElse(p2, "other2"), contains(params.getAll(p2).toArray()));
        assertThat(params.getAllOrElse(p3, "other3"), contains("other3"));
        assertThat(params.getAllOrElse(p4, "other4"), contains("other4"));
        expect(() -> params.getAllOrElse(notRegistered, "other4")).toThrow(IllegalArgumentException.class);

        assertThat(params.getAllOrElse(p1, ImmutableList.of("other1", "other2")), contains(params.getAll(p1).toArray()));
        assertThat(params.getAllOrElse(p2, ImmutableList.of("other3", "other4")), contains(params.getAll(p2).toArray()));
        assertThat(params.getAllOrElse(p3, ImmutableList.of("other5", "other6")), contains("other5", "other6"));
        assertThat(params.getAllOrElse(p3, ImmutableList.of("other7", "other8")), contains("other7", "other8"));
        expect(() -> params.getAllOrElse(notRegistered, ImmutableList.of("other7", "other8"))).toThrow(IllegalArgumentException.class);
    }

    @Test
    public void exists() {
        assertThat(params.exists(p1), is(true));
        assertThat(params.exists(p2), is(true));
        assertThat(params.exists(p3), is(false));
        assertThat(params.exists(p4), is(false));
        assertThat(params.exists(notRegistered), is(false));
    }

}
