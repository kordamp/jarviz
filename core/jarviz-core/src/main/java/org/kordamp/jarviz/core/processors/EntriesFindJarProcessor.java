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
import org.kordamp.jarviz.core.resolvers.JarFileResolver;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static org.kordamp.jarviz.util.StringUtils.isBlank;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class EntriesFindJarProcessor implements JarProcessor<Set<String>> {
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    private final JarFileResolver jarFileResolver;
    private String entryName;
    private String entryPattern;

    public EntriesFindJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getEntryPattern() {
        return entryPattern;
    }

    public void setEntryPattern(String entryPattern) {
        this.entryPattern = entryPattern;
    }

    @Override
    public Set<JarFileResult<Set<String>>> getResult() throws JarvizException {
        Set<JarFileResult<Set<String>>> set = new TreeSet<>();

        entryPattern = normalizePattern(entryPattern);
        for (JarFile jarFile : jarFileResolver.resolveJarFiles()) {
            set.add(processJarFile(jarFile, entryPattern));
        }

        return set;
    }

    protected JarFileResult<Set<String>> processJarFile(JarFile jarFile, String pattern) {
        Set<String> entries = new TreeSet<>();

        try (jarFile) {
            if (isNotBlank(pattern)) {
                try (FileSystem zipfs = FileSystems.newFileSystem(Path.of(jarFile.getName()),
                    this.getClass().getClassLoader())) {
                    PathMatcher pathMatcher = zipfs.getPathMatcher(pattern);
                    try (Stream<Path> stream = Files.walk(zipfs.getPath("/"))) {
                        stream.filter(pathMatcher::matches)
                            .forEach(path -> processJarEntry(jarFile, zipfs, path, entries));
                    }
                }
            } else {
                Enumeration<JarEntry> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = jarEntries.nextElement();
                    String name = entry.getName();
                    if (name.equals(entryName)) {
                        processJarEntry(jarFile, entry, entries);
                        break;
                    }
                }
            }

            return JarFileResult.of(jarFile, unmodifiableSet(entries));
        } catch (IOException ignored) {
            return JarFileResult.of(jarFile, unmodifiableSet(entries));
        }
    }

    protected void processJarEntry(JarFile jarFile, FileSystem zipfs, Path path, Set<String> entries) {
        String entryName = path.toString();
        if (entryName.startsWith("/")) {
            entryName = entryName.substring(1);
        }
        entries.add(entryName);
    }

    protected void processJarEntry(JarFile jarFile, JarEntry entry, Set<String> entries)  {
        String entryName = entry.getName();
        if (entryName.startsWith("/")) {
            entryName = entryName.substring(1);
        }
        entries.add(entryName);
    }

    private String normalizePattern(String pattern) {
        if (isBlank(pattern)) return pattern;

        if (!pattern.startsWith(GLOB_PREFIX) && !pattern.startsWith(REGEX_PREFIX)) {
            pattern = GLOB_PREFIX + pattern;
        }

        if (pattern.startsWith(GLOB_PREFIX)) {
            String path = pattern.substring(GLOB_PREFIX.length());
            if (path.startsWith("**") || path.startsWith("/")) {
                return pattern;
            } else {
                return GLOB_PREFIX + "**/" + path;
            }
        }

        return pattern;
    }
}
