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

import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class TxtFormatter extends Formatter {
    public static final TxtFormatter INSTANCE = new TxtFormatter();
    private static final String COLON = ":";

    private TxtFormatter() {
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
        report.indent(indentationFor(node))
            .append(element(node.getName()));

        if (isNotBlank(node.getValue())) {
            report.append(COLON)
                .append(SPACE)
                .append(formatValue(node));
        } else if (!node.getChildren().isEmpty()) {
            report.append(COLON)
                .newLine();
            for (Node child : node.getChildren()) {
                append(report, child);
            }
            report.indent(indentationFor(node));
        }

        report.newLine();
    }

    private String element(String str) {
        return str;
    }

    @Override
    protected int indentAdjustment() {
        return 1;
    }
}
