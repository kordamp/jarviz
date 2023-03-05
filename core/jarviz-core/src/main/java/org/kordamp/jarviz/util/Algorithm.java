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
package org.kordamp.jarviz.util;

import java.util.Locale;

import static org.kordamp.jarviz.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public enum Algorithm {
    MD5,
    SHA_1,
    SHA_256,
    SHA_512;

    public String formatted() {
        return name().toUpperCase(Locale.ENGLISH).replace("_", "-");
    }

    public static Algorithm of(String str) {
        if (isBlank(str)) return null;

        String value = str.toUpperCase(Locale.ENGLISH).trim()
            .replace("-", "_");

        switch (value) {
            case "SHA1":
                return SHA_1;
            case "SHA256":
                return SHA_256;
            case "SHA512":
                return SHA_512;
            default:
                // noop
        }

        return Algorithm.valueOf(value);
    }
}
