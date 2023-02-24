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
package org.kordamp.jarviz.core.model;

import java.util.Comparator;
import java.util.Objects;

import static java.util.Comparator.comparingInt;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class BytecodeVersion implements Comparable<BytecodeVersion> {
    private static final Comparator<BytecodeVersion> BYTECODE_VERSION_COMPARATOR = comparingInt(BytecodeVersion::getMajor)
        .thenComparing(BytecodeVersion::getMinor);

    private final int major;
    private final int minor;

    public static BytecodeVersion of(int major) {
        return new BytecodeVersion(major, 0);
    }

    public static BytecodeVersion of(int major, int minor) {
        return new BytecodeVersion(major, minor);
    }

    private BytecodeVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public boolean isEmpty() {
        return 0 == major && 0 == minor;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    public int compareTo(BytecodeVersion o) {
        if (null == o) return -1;
        return BYTECODE_VERSION_COMPARATOR.compare(this, o);
    }

    @Override
    public String toString() {
        return major + (0 != minor ? "." + minor : "") + asJavaVersion();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BytecodeVersion that = (BytecodeVersion) o;
        return major == that.major && minor == that.minor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor);
    }

    private String asJavaVersion() {
        // Java 1.1
        if (45 == major && 3 == minor) {
            return " (Java 1.1)";
        }

        int javaVersion = major - 44;
        if (65535 == minor) {
            return " (Java " + javaVersion + "-preview)";
        } else {
            return " (Java " + javaVersion + ")";
        }
    }
}
