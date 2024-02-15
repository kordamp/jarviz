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
import java.util.List;

import static java.lang.System.lineSeparator;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Report {
    protected static final String INDENT = "  ";

    private final List<String> lines = new ArrayList<>();
    private String current = "";

    public Report indent(int times) {
        current = INDENT.repeat(Math.max(0, times));
        return this;
    }

    public Report append(String str) {
        current += str;
        return this;
    }

    public Report append(char c) {
        current += c;
        return this;
    }

    public Report newLine() {
        if (isNotBlank(current)) {
            lines.add(current);
        }

        current = "";

        return this;
    }

    @Override
    public String toString() {
        return String.join(lineSeparator(), lines).trim() + lineSeparator();
    }
}
