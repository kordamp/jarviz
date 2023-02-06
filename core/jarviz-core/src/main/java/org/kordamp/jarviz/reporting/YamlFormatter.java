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
import static org.kordamp.jarviz.util.StringUtils.isBlank;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class YamlFormatter extends Formatter {
    public static final YamlFormatter INSTANCE = new YamlFormatter();
    private static final String COLON = ":";
    private static final String ITEM = "- ";
    private static final String MULTILINE = "|";

    private YamlFormatter() {
        // noop
    }

    @Override
    public String write(Node node) {
        Report report = new Report();
        for (Node child : node.getChildren()) {
            append(report, child);
        }
        return report.toString();
    }

    @Override
    protected void append(Report report, Node node) {
        if (isBlank(node.getValue()) && node.getChildren().isEmpty()) return;

        if (!node.isCollapsable()) {
            report.indent(indentationFor(node))
                .append(node.isArrayElement() ? (node.isFirstChild() ? ITEM : EMPTY) : EMPTY)
                .append(element(node.getName()))
                .append(COLON);
        }

        if (isNotBlank(node.getValue())) {
            report.append(node.isCollapsable() ? INDENT.repeat(indentationFor(node)) + ITEM : SPACE)
                .append(formatValue(node));
            if (node.isCollapsable()) report.newLine();
        } else if (!node.getChildren().isEmpty()) {
            if (!node.isCollapsable()) report.newLine();
            for (Node child : node.getChildren()) {
                append(report, child);
            }
            if (!node.isCollapsable()) report.newLine();
        }

        if (!node.isCollapsable()) report.newLine();
    }

    @Override
    protected int indentationFor(Node node, int offset) {
        if (node.getParent().isPresent()) {
            offset += node.getParent().get().isCollapsable() && node.isArrayElement() && node.isFirstChild() ? -(node.getIndentation() / 2) + 1 : 0;
        }
        return super.indentationFor(node, offset);
    }

    private String element(String str) {
        return str;
    }

    @Override
    protected int indentAdjustment() {
        return 1;
    }

    @Override
    protected String formatValue(Node node) {
        String value = node.getValue();
        if (!value.contains(lineSeparator())) return formatValue(value);

        StringBuilder b = new StringBuilder()
            .append(MULTILINE)
            .append(lineSeparator());

        String[] lines = value.split("\\r");
        String indentation = INDENT.repeat(indentationFor(node, 1));
        for (String line : lines) {
            b.append(indentation)
                .append(line.trim())
                .append(lineSeparator());
        }

        return b.toString();
    }
}
