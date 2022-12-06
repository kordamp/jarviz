/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 The Jarviz authors.
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

import org.apache.commons.io.function.IOFunction;
import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarvizException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JarUtils {
    private static final Map<String, Integer> JDK_TO_MAJOR_VERSION_NUMBER_MAPPING = new LinkedHashMap<>();

    static {
        for (int i = 6; i < 35; i++) {
            JDK_TO_MAJOR_VERSION_NUMBER_MAPPING.put(String.valueOf(i), 44 + i);
        }
    }

    public static Integer javaVersionToBytecodeVersion(String jv) {
        return JDK_TO_MAJOR_VERSION_NUMBER_MAPPING.get(jv);
    }

    public static Manifest getManifest(JarFile jarFile) {
        try {
            return jarFile.getManifest();
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_READING_JAR_MANIFEST", jarFile.getName()));
        }
    }

    public static <T> T withJarEntry(JarFile jarFile, JarEntry entry, IOFunction<InputStream, T> consumer) throws IOException {
        try (InputStream is = jarFile.getInputStream(entry)) {
            return consumer.apply(is);
        }
    }

    public static Integer readMajorVersion(JarFile jarFile, JarEntry entry) {
        try {
            return JarUtils.withJarEntry(jarFile, entry, inputStream -> {
                byte[] magicAndClassFileVersion = new byte[8];
                int total = magicAndClassFileVersion.length;
                while (total > 0) {
                    int read = inputStream.read(magicAndClassFileVersion, magicAndClassFileVersion.length - total, total);
                    if (read == -1) {
                        throw new EOFException(jarFile.getName());
                    }

                    total -= read;
                }
                return (magicAndClassFileVersion[6] << 8) + magicAndClassFileVersion[7];
            });
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_READING_JAR_ENTRY", entry.getName(), jarFile.getName()));
        }
    }
}
