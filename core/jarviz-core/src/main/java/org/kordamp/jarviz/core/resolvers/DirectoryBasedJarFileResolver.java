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
package org.kordamp.jarviz.core.resolvers;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarvizException;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class DirectoryBasedJarFileResolver implements JarFileResolver {
    private final Path directory;
    private Set<JarFile> jarFiles;

    public DirectoryBasedJarFileResolver(Path directory) {
        this.directory = directory;
    }

    @Override
    public Set<JarFile> resolveJarFiles() {
        if (null != jarFiles) return jarFiles;

        Set<JarFile> set = new LinkedHashSet<>();

        GlobResolver resolver = new GlobResolver();
        try {
            java.nio.file.Files.walkFileTree(directory, resolver);
            if (resolver.failed) {
                throw new JarvizException(RB.$("ERROR_WALK_DIRECTORY", directory));
            }

            for (Path file : resolver.paths) {
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
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_WALK_DIRECTORY", directory), e);
        }

        jarFiles = set;
        return jarFiles;
    }

    private static class GlobResolver extends SimpleFileVisitor<Path> {
        private final PathMatcher matcher;
        private final Set<Path> paths = new LinkedHashSet<>();
        private boolean failed;

        private GlobResolver() {
            this.matcher = FileSystems.getDefault().getPathMatcher("glob:**/*.jar");
        }

        private void match(Path path) {
            if (matcher.matches(path)) {
                paths.add(path);
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            match(file);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
            failed = true;
            return CONTINUE;
        }
    }
}
