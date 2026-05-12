/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PathParamsTest {

    private final PathParamRule<String> p1 = PathParamRule.Companion.of("p1", ParamType.INSTANCE.getSTRING());
    private final PathParamRule<Integer> p2 = PathParamRule.Companion.of("p2", ParamType.INSTANCE.getINT());
    private final PathParamRule<Integer> p3 = PathParamRule.Companion.of("p3", ParamType.INSTANCE.getINT());
    private final PathParamRule<String> notRegistered = PathParamRule.Companion.of("none", ParamType.INSTANCE.getSTRING());

    private final PathParams params = new PathParams(ImmutableMap.of(p1.getName(), "aString", p2.getName(), 4));

    @Test
    public void get() {
        assertThat(params.get(p1), is("aString"));
        assertThat(params.get(p2), is(4));
        expect(() -> params.get(p3)).toThrow(IllegalArgumentException.class);
        expect(() -> params.get(notRegistered)).toThrow(IllegalArgumentException.class);
    }

    @Test
    public void exists() {
        assertThat(params.exists(p1), is(true));
        assertThat(params.exists(p2), is(true));
        assertThat(params.exists(p3), is(false));
        assertThat(params.exists(notRegistered), is(false));
    }
}
