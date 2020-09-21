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
import com.zepben.vertxutils.json.filter.parser.Parser;

import java.util.Optional;

/**
 * <b>FilterSpecification</b>
 * <p>
 * Specifies json fields to filter. Can be used to specify fields to exclude or to specify fields to include (i.e., exclude everything but).
 * <p>
 * For example: a.b.c or a.b.c,x.y.x or a(b,c(d)) or -a.b.c or -a.b.c,-x.y.x or -a(b,c(d))
 * <p>
 */
@EverythingIsNonnullByDefault
@SuppressWarnings({"WeakerAccess"})
public class FilterSpecification {

    private Node root;

    public FilterSpecification() {
        root = new Node();
    }

    public FilterSpecification(String filter) throws FilterException {
        root = Parser.parse(filter);
    }

    private FilterSpecification(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public String getFilter() {
        return root.toString();
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public Optional<FilterSpecification> getSubfilter(String location) {
        Node descendant = root.getDescendent(location);
        if (descendant == null)
            return Optional.empty();
        else
            return Optional.of(new FilterSpecification(descendant));
    }

}
