/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json.filter;

import com.zepben.testutils.junit.SystemLogExtension;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JsonObjectFilterTest {

    @RegisterExtension
    SystemLogExtension systemOut = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess();

    @Test
    public void filtersObject() throws Exception {
        JsonObject jsonObject = filteredObject("key1,key3(key31,key32)");

        validateTopLevelKeys(jsonObject, true, false, true, false);
        JsonObject key3 = jsonObject.getJsonObject("key3");

        assertThat(key3.containsKey("key31"), equalTo(true));
        assertThat(key3.containsKey("key32"), equalTo(true));
        assertThat(key3.containsKey("key33"), equalTo(false));

        jsonObject = filteredObject("key2,key4(key41,key43(key431,key433))");

        validateTopLevelKeys(jsonObject, false, true, false, true);
        validateKey4(jsonObject, true, true, false);

        jsonObject = filteredObject("key4(key43.key431,key44.key442)");

        validateTopLevelKeys(jsonObject, false, false, false, true);
        validateKey4(jsonObject, false, false, true);

        jsonObject = filteredObject("key1.value");
        validateTopLevelKeys(jsonObject, true, false, false, false);

        jsonObject = filteredObject("-key1.value");
        validateTopLevelKeys(jsonObject, true, true, true, true);
    }

    private void validateKey4(JsonObject jsonObject, boolean expect41, boolean expect433, boolean expect44) {
        JsonArray key4 = jsonObject.getJsonArray("key4");

        assertThat(key4.size(), equalTo(2));
        validateArrayObjects(key4.getJsonArray(0), 3, 1, expect41, expect433, expect44);
        validateArrayObjects(key4.getJsonArray(1), 1, 4, expect41, expect433, expect44);
    }

    @Test
    public void filtersDoNotRemoveEmptyArrays() throws Exception {
        JsonObject jsonObject = createObject();
        jsonObject.getJsonArray("key4").clear();

        JsonObjectFilter.applyFilter(jsonObject, new FilterSpecification("key4.key43.key431"));

        validateTopLevelKeys(jsonObject, false, false, false, true);
        JsonArray key4 = jsonObject.getJsonArray("key4");

        assertThat(key4.size(), equalTo(0));
    }

    @Test
    public void isFluent() throws Exception {
        JsonObject jsonObject = new JsonObject();
        assertThat(JsonObjectFilter.applyFilter(jsonObject, new FilterSpecification("test")), equalTo(jsonObject));
    }

    private void validateTopLevelKeys(JsonObject jsonObject, boolean expect1, boolean expect2, boolean expect3, boolean expect4) {
        assertThat(jsonObject.containsKey("key1"), equalTo(expect1));
        assertThat(jsonObject.containsKey("key2"), equalTo(expect2));
        assertThat(jsonObject.containsKey("key3"), equalTo(expect3));
        assertThat(jsonObject.containsKey("key4"), equalTo(expect4));
    }

    private void validateArrayObjects(JsonArray jsonArray, int expectedSize, int startCount, boolean expect41, boolean expect433, boolean expect44) {
        assertThat(jsonArray.size(), equalTo(expectedSize));

        for (int i = 0; i < expectedSize; ++i)
            validateArrayObject(jsonArray.getJsonObject(i), startCount + i, expect41, expect433, expect44);
    }

    private void validateArrayObject(JsonObject jsonObject, double count, boolean expect41, boolean expect433, boolean expect44) {
        assertThat(jsonObject.containsKey("key41"), equalTo(expect41));
        assertThat(jsonObject.containsKey("key42"), equalTo(false));
        assertThat(jsonObject.containsKey("key43"), equalTo(true));
        assertThat(jsonObject.containsKey("key44"), equalTo(expect44));

        if (expect41)
            assertThat(jsonObject.getDouble("key41"), equalTo(41 + (count / 10)));

        JsonObject key43 = jsonObject.getJsonObject("key43");

        assertThat(key43.containsKey("key431"), equalTo(true));
        assertThat(key43.containsKey("key432"), equalTo(false));
        assertThat(key43.containsKey("key433"), equalTo(expect433));

        assertThat(key43.getDouble("key431"), equalTo(431 + (count / 10)));
        if (expect433)
            assertThat(key43.getDouble("key433"), equalTo(433 + (count / 10)));

        if (expect44) {
            JsonObject key44 = jsonObject.getJsonObject("key44");
            assertThat(key44.containsKey("key441"), equalTo(false));
            assertThat(key44.containsKey("key442"), equalTo(true));
            assertThat(key44.containsKey("key443"), equalTo(false));

            assertThat(key44.getDouble("key442"), equalTo(442 + (count / 10)));
        }
    }

    private JsonObject filteredObject(String filter) throws Exception {
        JsonObject jsonObject = createObject();
        JsonObjectFilter.applyFilter(jsonObject, new FilterSpecification(filter));
        return jsonObject;
    }

    private JsonObject createObject() {
        return new JsonObject()
            .put("key1", 1)
            .put("key2", "2")
            .put("key3", createObject(3, 1))
            .put("key4", new JsonArray()
                .add(new JsonArray()
                    .add(createObject(4, 1))
                    .add(createObject(4, 2))
                    .add(createObject(4, 3))
                )
                .add(new JsonArray()
                    .add(createObject(4, 4))
                )
            );
    }

    private JsonObject createObject(int key, double count) {
        return new JsonObject()
            .put("key" + key + "1", (key * 10) + 1 + (count / 10))
            .put("key" + key + "2", (key * 10) + 2 + (count / 10))
            .put("key" + key + "3", new JsonObject()
                .put("key" + key + "31", (key * 100) + 31 + (count / 10))
                .put("key" + key + "32", (key * 100) + 32 + (count / 10))
                .put("key" + key + "33", (key * 100) + 33 + (count / 10))
            )
            .put("key" + key + "4", new JsonObject()
                .put("key" + key + "41", (key * 100) + 41 + (count / 10))
                .put("key" + key + "42", (key * 100) + 42 + (count / 10))
                .put("key" + key + "43", (key * 100) + 43 + (count / 10))
            );
    }

}
