/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022-2023 The Jarviz authors.
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

import static java.lang.System.lineSeparator;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class Formatter {
    protected static final String INDENT = "  ";
    protected static final String SPACE = " ";
    protected static final String EMPTY = "";

    public String write(Node node) {
        Report report = new Report();
        append(report, node);
        return report.toString();
    }

    protected abstract void append(Report report, Node node);

    protected int indentAdjustment() {
        return 0;
    }

    protected int indentationFor(Node node) {
        return indentationFor(node, 0);
    }

    protected int indentationFor(Node node, int offset) {
        return Math.max(0, node.getIndentation() - indentAdjustment() + offset);
    }

    protected String formatValue(Node node) {
        String value = node.getValue();
        if (!value.contains(lineSeparator())) return formatValue(value);

        String indentation = INDENT.repeat(indentationFor(node, 1));
        StringBuilder b = new StringBuilder(lineSeparator());
        for (String line : value.split(lineSeparator())) {
            b.append(indentation)
                .append(formatValue(line))
                .append(lineSeparator());
        }

        return b.toString();
    }

    protected String formatValue(String value) {
        return value.trim();
    }
}
