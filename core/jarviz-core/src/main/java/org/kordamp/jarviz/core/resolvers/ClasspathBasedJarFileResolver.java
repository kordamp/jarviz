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
package org.kordamp.jarviz.core.resolvers;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarvizException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ClasspathBasedJarFileResolver implements JarFileResolver {
    private final String classpath;
    private Set<JarFile> jarFiles;

    public ClasspathBasedJarFileResolver(String classpath) {
        this.classpath = classpath;
    }

    @Override
    public Set<JarFile> resolveJarFiles() {
        if (null != jarFiles) return jarFiles;

        Set<JarFile> set = new LinkedHashSet<>();
        for (String s : classpath.split(File.pathSeparator)) {
            Path file = Path.of(s);

            if (Files.notExists(file)) {
                continue;
            }
            if (!Files.isRegularFile(file)) {
                throw new JarvizException(RB.$("ERROR_PATH_IS_NOT_A_FILE", file.toAbsolutePath()));
            }
            if (!Files.isReadable(file)) {
                throw new JarvizException(RB.$("ERROR_PATH_IS_NOT_READABLE", file.toAbsolutePath()));
            }
            if (!file.getFileName().toString().endsWith(".jar")) {
                throw new JarvizException(RB.$("ERROR_PATH_IS_NOT_JAR", file.toAbsolutePath()));
            }

            try {
                set.add(new JarFile(file.toFile()));
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_OPENING_JAR", file.toAbsolutePath()));
            }
        }

        jarFiles = set;
        return jarFiles;
    }
}
