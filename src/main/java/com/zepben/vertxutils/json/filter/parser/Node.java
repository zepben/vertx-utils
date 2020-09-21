/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json.filter.parser;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.TreeMap;

import static com.zepben.vertxutils.json.filter.parser.FilterType.EXCLUDE;
import static com.zepben.vertxutils.json.filter.parser.FilterType.PASSTHROUGH;

@SuppressWarnings({"WeakerAccess"})
public class Node {

    // The children of this Node (might be empty)
    private final TreeMap<String, Node> children = new TreeMap<>();

    // The string content of this node if there is any
    private String content;

    // Is this an exclude or include node?
    private FilterType filterType;

    public Node() {
        filterType = PASSTHROUGH;
    }

    public Node(String content) {
        this.content = content;
    }

    public String content() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public FilterType filterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public Collection<Node> children() {
        return children.values();
    }

    public Node getChild(String name) {
        return children.get(name);
    }

    public Node getDescendent(String name) {
        Node descendant = this;
        for (String n : name.split("\\.")) {
            if (descendant == null) {
                return null;
            }
            descendant = descendant.getChild(n);
        }
        return descendant;
    }

    public Node addOrGetChild(String content) {
        Node child = children.get(content);
        if (child == null) {
            child = new Node(content);
            child.setFilterType(filterType);
            children.put(content, child);
        }
        return child;
    }

    public int countAllNodes() {
        int n = 1;
        for (Node child : children.values()) {
            n += child.countAllNodes();
        }
        return n;
    }

    @Override
    public String toString() {
        String childrenString = StringUtils.join(children.values(), ",").replaceAll("-", "");
        if (content == null) {
            return (filterType == EXCLUDE ? "-" : "") + childrenString;
        }
        if (children.isEmpty()) {
            return content;
        } else {
            return String.format(children.size() == 1 ? "%s.%s" : "%s(%s)", content, childrenString);
        }
    }

}
