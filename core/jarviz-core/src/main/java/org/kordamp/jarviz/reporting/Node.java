/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022-2024 The Jarviz authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kordamp.jarviz.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;
import static org.kordamp.jarviz.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Node {
    private final Node parent;
    private final int indentation;
    private final String name;
    private final List<Node> children = new ArrayList<>();
    private final boolean array;
    private final boolean collapsable;
    private Object value;

    private Node(Node parent, String name, boolean array, boolean collapsable) {
        this.parent = parent;
        this.name = name;
        this.array = array;
        this.collapsable = collapsable;
        this.indentation = null != parent ? parent.indentation + 1 : 0;
    }

    public int getIndentation() {
        return indentation;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return null != value ? String.valueOf(value) : null;
    }

    public List<Node> getChildren() {
        return unmodifiableList(children);
    }

    public Optional<Node> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean isCollapsable() {
        return collapsable;
    }

    public boolean isArray() {
        return array;
    }

    public boolean isArrayElement() {
        return null != parent && parent.isArray() ||
            null != parent && parent.isCollapsable();
    }

    public boolean isFirstChild() {
        return null != parent && parent.children.indexOf(this) == 0;
    }

    public Node value(Object value) {
        this.value = value;
        return this;
    }

    public Node node(String name) {
        Node child = new Node(this, name, false, false);
        children.add(child);
        return child;
    }

    public Node array(String name) {
        Node child = new Node(this, name, true, false);
        children.add(child);
        return child;
    }

    public Node collapsable(String name) {
        Node child = new Node(this, name, false, true);
        children.add(child);
        return child;
    }

    public Node children(Collection<String> elements) {
        for (String e : elements) {
            this.node(e);
        }
        return this;
    }

    public Node children(String name, Collection<String> elements) {
        for (String e : elements) {
            this.node(name).value(e);
        }
        return this;
    }

    public Node collapsableChildren(String name, Collection<String> elements) {
        for (String e : elements) {
            this.collapsable(name).value(e);
        }
        return this;
    }

    public Node cleanup() {
        children.removeIf(child -> isBlank(child.getValue()) && child.getChildren().isEmpty());
        return null != parent ? parent : this;
    }

    public Node end() {
        return null != parent ? parent : this;
    }

    public static Node root(String name) {
        return new Node(null, name, false, false);
    }
}
