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

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.model.BytecodeVersion;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JarUtils {
    public static Optional<Manifest> getManifest(JarFile jarFile) {
        try {
            return Optional.ofNullable(jarFile.getManifest());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static <T> T withJarEntry(JarFile jarFile, JarEntry entry, IOFunction<InputStream, T> consumer) throws IOException {
        try (InputStream is = jarFile.getInputStream(entry)) {
            return consumer.apply(is);
        }
    }

    public static BytecodeVersion readBytecodeVersion(JarFile jarFile, JarEntry entry) {
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

                int minor = (magicAndClassFileVersion[4] << 8) + magicAndClassFileVersion[5];
                int major = (magicAndClassFileVersion[6] << 8) + magicAndClassFileVersion[7];
                return BytecodeVersion.of(major, minor);
            });
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_READING_JAR_ENTRY", entry.getName(), jarFile.getName()));
        }
    }

    @FunctionalInterface
    public interface IOFunction<T, R> {
        R apply(final T t) throws IOException;
    }
}
