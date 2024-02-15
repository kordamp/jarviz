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

import static java.lang.System.lineSeparator;
import static org.kordamp.jarviz.util.StringUtils.isBlank;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class JsonFormatter extends Formatter {
    public static final JsonFormatter INSTANCE = new JsonFormatter();

    private static final char OPEN_STRUCT = '{';
    private static final char CLOSE_STRUCT = '}';
    private static final char OPEN_ARRAY = '[';
    private static final char CLOSE_ARRAY = ']';
    private static final char QUOTES = '"';
    private static final String COLON = ":";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final char COMMA = ',';

    private JsonFormatter() {
        // noop
    }

    @Override
    public String write(Node node) {
        Report report = new Report()
            .append(OPEN_STRUCT)
            .newLine();
        appendChildren(report, node);
        report.append(CLOSE_STRUCT)
            .newLine();
        return report.toString();
    }

    @Override
    protected void append(Report report, Node node) {
        if (isBlank(node.getValue()) && node.getChildren().isEmpty()) {
            report.indent(indentationFor(node))
                .append(quote(node.getName()));
            return;
        }

        if (!node.isCollapsable()) {
            report.indent(indentationFor(node))
                .append(quote(node.getName()))
                .append(COLON)
                .append(SPACE);
        } else {
            report.indent(indentationFor(node));
        }

        if (isNotBlank(node.getValue())) {
            report.append(formatValue(node));
        } else if (!node.getChildren().isEmpty()) {
            report.append(node.isArray() ? OPEN_ARRAY : OPEN_STRUCT)
                .newLine();
            appendChildren(report, node);
            report.indent(indentationFor(node))
                .append(node.isArray() ? CLOSE_ARRAY : CLOSE_STRUCT);
        }
    }

    private void appendChildren(Report report, Node node) {
        int childCount = node.getChildren().size();
        for (int i = 0; i < childCount; i++) {
            Node child = node.getChildren().get(i);
            append(report, child);
            if (childCount > 1 && i != childCount - 1) {
                report.append(COMMA);
            }
            report.newLine();
        }
    }

    @Override
    protected String formatValue(Node node) {
        String value = node.getValue();
        if (!value.contains(lineSeparator())) return formatValue(value);

        StringBuilder b = new StringBuilder()
            .append(OPEN_ARRAY)
            .append(lineSeparator());

        String[] lines = value.split("\\r");
        String indentation = INDENT.repeat(indentationFor(node, 1));
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            b.append(indentation)
                .append(quote(line));
            if (lines.length > 1 && i != lines.length - 1) {
                b.append(COMMA);
            }
            b.append(lineSeparator());
        }

        return b.append(INDENT.repeat(indentationFor(node)))
            .append(CLOSE_ARRAY)
            .toString();
    }

    @Override
    protected String formatValue(String value) {
        if (isBoolean(value) || isNumber(value)) return value;
        return quote(value.trim());
    }

    private boolean isBoolean(String value) {
        return TRUE.equals(value) || FALSE.equals(value);
    }

    private boolean isNumber(String value) {
        return isDouble(value) || isLong(value);
    }

    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String quote(String value) {
        return QUOTES + value.replaceAll("\"", "\\\\\"") + QUOTES;
    }
}
