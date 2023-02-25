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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarFile;

import static java.util.Collections.singleton;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class PathBasedJarFileResolver implements JarFileResolver {
    private final Path file;
    private JarFile jarFile;

    public PathBasedJarFileResolver(Path file) {
        this.file = file;
    }

    @Override
    public Set<JarFile> resolveJarFiles() {
        if (null != jarFile) return singleton(jarFile);

        if (Files.notExists(file)) {
            throw new JarvizException(RB.$("ERROR_PATH_DOES_NOT_EXIST", file.toAbsolutePath()));
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
            jarFile = new JarFile(file.toFile());
            return singleton(jarFile);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_OPENING_JAR", file.toAbsolutePath()));
        }
    }
}
