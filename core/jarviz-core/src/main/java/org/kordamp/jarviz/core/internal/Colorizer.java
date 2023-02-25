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
package org.kordamp.jarviz.core.internal;

import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Colorizer {
    private Colorizer() {
        // noop
    }

    public static String bool(boolean b) {
        return b ? green("true") : red("false");
    }

    public static String bool(String str) {
        return Boolean.parseBoolean(str) ? green(str) : red(str);
    }

    public static String red(String input) {
        return colorize("red", input);
    }

    public static String cyan(String input) {
        return colorize("cyan", input);
    }

    public static String green(String input) {
        return colorize("green", input);
    }

    public static String yellow(String input) {
        return colorize("yellow", input);
    }

    public static String magenta(String input) {
        return colorize("magenta", input);
    }

    public static String colorize(String input) {
        return CommandLine.Help.Ansi.AUTO.string(input);
    }

    public static String colorize(String color, String input) {
        return CommandLine.Help.Ansi.AUTO.string("@|" + color + " " + input + "|@");
    }
}
