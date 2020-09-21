/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json.filter;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import com.zepben.vertxutils.json.filter.parser.Node;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@EverythingIsNonnullByDefault
@SuppressWarnings("WeakerAccess")
public class JsonObjectFilter implements JsonFilter<JsonObject, JsonObject> {

    static public JsonObject applyFilter(JsonObject object, FilterSpecification fs) {
        return new JsonObjectFilter().apply(object, fs);
    }

    /*
     * Applies a filter to a JsonObject. Note that this mutates the given object and returns it.
     */
    @Override
    public JsonObject apply(JsonObject object, FilterSpecification fs) {
        apply(fs.getRoot(), object);
        return object;
    }

    private void apply(Node node, Object object) {
        switch (node.filterType()) {
            case INCLUDE:
                includeSpecified(node, object);
                break;
            case EXCLUDE:
                excludeSpecified(node, object);
                break;
            case PASSTHROUGH:
                // Do nothing
                break;
        }
    }

    private void excludeSpecified(Node node, Object object) {
        if (object instanceof JsonArray) {
            for (Object value : ((JsonArray) object)) {
                apply(node, value);
            }
        } else if (object instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) object;
            for (Node child : node.children()) {
                if (child.children().isEmpty()) {
                    jsonObject.remove(child.content());
                } else {
                    apply(child, jsonObject.getValue(child.content()));
                }
            }
        }
    }

    private void includeSpecified(Node node, Object object) {
        if (node.children().isEmpty()) {
            return;  // Nothing to do
        }
        if (object instanceof JsonArray) {
            for (Object value : ((JsonArray) object)) {
                apply(node, value);
            }
        } else if (object instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) object;

            Set<String> fieldsToInclude = node
                .children()
                .stream()
                .map(Node::content)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            Set<String> fieldsToRemove = new HashSet<>(jsonObject.fieldNames());
            fieldsToRemove.removeAll(fieldsToInclude);

            for (String fieldName : fieldsToRemove) {
                jsonObject.remove(fieldName);
            }

            for (Node child : node.children()) {
                String fieldName = child.content();
                Object value = jsonObject.getValue(fieldName);
                if (value != null) {
                    apply(child, value);
                }
            }
        }
    }

}
