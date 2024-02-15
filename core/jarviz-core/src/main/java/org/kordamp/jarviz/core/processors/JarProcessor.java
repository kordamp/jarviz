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
package org.kordamp.jarviz.core.processors;

import org.kordamp.jarviz.core.JarvizException;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface JarProcessor<R> {
    Set<JarFileResult<R>> getResult() throws JarvizException;

    class JarFileResult<R> implements Comparable<JarFileResult<R>> {
        private final JarFile jarFile;
        private final R result;

        public static <R> JarFileResult<R> of(JarFile jarFile, R result) {
            return new JarFileResult<>(jarFile, result);
        }

        private JarFileResult(JarFile jarFile, R result) {
            this.jarFile = jarFile;
            this.result = result;
        }

        public JarFile getJarFile() {
            return jarFile;
        }

        public R getResult() {
            return result;
        }

        public Path getJarPath() {
            return Path.of(jarFile.getName());
        }

        public String getJarFileName() {
            return Path.of(jarFile.getName()).getFileName().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JarFileResult<?> result = (JarFileResult<?>) o;
            return getJarFileName().equals(result.getJarFileName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getJarFileName());
        }

        @Override
        public int compareTo(JarFileResult<R> o) {
            if (null == o) return -1;
            return getJarFileName().compareTo(o.getJarFileName());
        }
    }
}
