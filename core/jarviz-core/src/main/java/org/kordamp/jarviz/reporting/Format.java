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

import java.util.Locale;

import static org.kordamp.jarviz.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public enum Format {
    TXT,
    JSON,
    YAML,
    XML;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public static Format of(String str) {
        if (isBlank(str)) return null;
        return Format.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
    }
}
