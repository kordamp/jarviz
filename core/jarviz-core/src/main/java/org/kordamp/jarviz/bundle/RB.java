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
package org.kordamp.jarviz.bundle;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class RB {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.kordamp.jarviz.bundle.Messages");

    private RB() {
        // noop
    }

    public static String $(String key, Object... args) {
        if (null == args || args.length == 0) {
            return BUNDLE.getString(key);
        }
        return MessageFormat.format(BUNDLE.getString(key), args);
    }
}
