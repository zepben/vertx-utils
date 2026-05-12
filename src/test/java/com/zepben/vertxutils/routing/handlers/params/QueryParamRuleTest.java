/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class QueryParamRuleTest {

    private final RequestValueConverter<String, String> converter = RequestValueConverter.Companion.create("string", s -> s);

    @Test
    public void basicConstructor() {
        QueryParamRule<String> rule = QueryParamRule.Companion.of("test", converter);
        assertThat(rule.getName(), is("test"));
        assertThat(rule.getConverter(), is(converter));
        assertThat(rule.isRequired(), is(false));
        assertThat(rule.getDefaultValue(), is(nullValue()));
    }

    @Test
    public void defaultValueConstructor() {
        QueryParamRule<String> rule = QueryParamRule.Companion.of("test", converter, "default");
        assertThat(rule.getName(), is("test"));
        assertThat(rule.getConverter(), is(converter));
        assertThat(rule.isRequired(), is(false));
        assertThat(rule.getDefaultValue(), is("default"));
    }

    @Test
    public void isRequiredConstructor() {
        QueryParamRule<String> rule = QueryParamRule.Companion.ofRequired("test", converter);
        assertThat(rule.getName(), is("test"));
        assertThat(rule.getConverter(), is(converter));
        assertThat(rule.isRequired(), is(true));
        assertThat(rule.getDefaultValue(), is(nullValue()));
    }

}
