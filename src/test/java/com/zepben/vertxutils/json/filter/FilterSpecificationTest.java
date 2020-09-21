/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json.filter;

import com.zepben.testutils.junit.SystemLogExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FilterSpecificationTest {

    @RegisterExtension
    SystemLogExtension systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess();

    @Test
    public void parsesCorrectly() throws Exception {
        String filter = "key1(key11.key111,key12.key121)";
        validateFilter(filter, filter);

        validateFilter("key1.key11,key1.key12", "key1(key11,key12)");
    }

    private void validateFilter(String filter, String expected) throws FilterException {
        FilterSpecification filterSpecification = new FilterSpecification(filter);
        assertThat(filterSpecification.toString(), equalTo(expected));
    }

}
