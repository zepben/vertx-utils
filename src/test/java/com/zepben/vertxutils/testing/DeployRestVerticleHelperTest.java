/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.testing;

import com.zepben.testutils.junit.SystemLogExtension;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.zepben.testutils.exception.ExpectException.expect;

public class DeployRestVerticleHelperTest {

    @RegisterExtension
    public final SystemLogExtension systemErr = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess();

    @AfterEach
    void afterEach() {
        TestVerticle.setOnStop(() -> {});
        TestVerticle.setOnStart(() -> {});
    }

    @Test
    public void coverageOnlyTest() throws Exception {
        // TODO: Make this an actual test.
        DeployRestVerticleHelper helper = buildHelper();
        helper.requestSpec();
        helper.getRandomPortNumber();
        helper.close();

        TestVerticle.setOnStart((promise) -> promise.fail("test start fail"));
        expect(this::buildHelper).toThrow(AssertionError.class);
    }

    private DeployRestVerticleHelper buildHelper() {
        return new DeployRestVerticleHelper(TestVerticle.class, new JsonObject());
    }

}
