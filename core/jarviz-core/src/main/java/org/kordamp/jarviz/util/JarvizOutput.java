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

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public final class JarvizOutput {
    public static final String JARVIZ_QUIET = "jarviz.quiet";

    private JarvizOutput() {
        // noop
    }

    public static boolean isQuiet() {
        return Boolean.getBoolean(JARVIZ_QUIET);
    }

    public static void nag(String msg) {
        if (!isQuiet()) {
            System.out.println(msg);
        }
    }
}
