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
public class XmlFormatter extends Formatter {
    public static final XmlFormatter INSTANCE = new XmlFormatter();

    private static final String OPEN = "<";
    private static final String OPEN_SLASH = "</";
    private static final String CLOSE = ">";
    private static final String CDATA_OPEN = "<![CDATA[";
    private static final String CDATA_CLOSE = "]]>";

    private XmlFormatter() {
        // noop
    }

    @Override
    protected void append(Report report, Node node) {
        if (isBlank(node.getValue()) && node.getChildren().isEmpty()) return;

        report.indent(indentationFor(node))
            .append(openXmlElement(node.getName()));

        if (isNotBlank(node.getValue())) {
            report.append(formatValue(node));
        } else {
            report.newLine();
            for (Node child : node.getChildren()) {
                append(report, child);
            }
            report.indent(indentationFor(node));
        }

        report.append(closeXmlElement(node.getName()))
            .newLine();
    }

    @Override
    protected String formatValue(Node node) {
        String value = node.getValue();
        if (!value.contains(lineSeparator())) return formatValue(value);

        String indentation = INDENT.repeat(indentationFor(node, 1));
        StringBuilder b = new StringBuilder(lineSeparator());
        for (String line : value.split(lineSeparator())) {
            b.append(indentation)
                .append(line)
                .append(lineSeparator());
        }

        return formatValue(b.toString());
    }

    private String openXmlElement(String str) {
        return OPEN + str + CLOSE;
    }

    private String closeXmlElement(String str) {
        return OPEN_SLASH + str + CLOSE;
    }

    protected String formatValue(String value) {
        return value.contains(OPEN) || value.contains(CLOSE) ? cdata(value) : value.trim();
    }

    private String cdata(String value) {
        return CDATA_OPEN + value + CDATA_CLOSE;
    }
}
