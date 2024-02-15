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

import org.kordamp.jarviz.core.resolvers.JarFileResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class EntriesExtractJarProcessor extends EntriesFindJarProcessor {
    private boolean flatten;
    private Path targetDirectory;

    public EntriesExtractJarProcessor(JarFileResolver jarFileResolver) {
        super(jarFileResolver);
    }

    public boolean isFlatten() {
        return flatten;
    }

    public void setFlatten(boolean flatten) {
        this.flatten = flatten;
    }

    public Path getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(Path targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Override
    protected void processJarEntry(JarFile jarFile, FileSystem zipfs, Path path, Set<String> entries) {
        if (Files.isDirectory(path)) return;

        String entryName = path.toString();
        if (entryName.startsWith("/")) {
            entryName = entryName.substring(1);
        }
        if (isFlatten()) {
            entryName = Paths.get(entryName).getFileName().toString();
        }

        String fileName = Path.of(jarFile.getName()).getFileName().toString();
        fileName = fileName.substring(0, fileName.length() - 4);
        Path destination = targetDirectory.resolve(fileName).resolve(entryName);

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(path, destination, REPLACE_EXISTING);
        } catch (IOException e) {
            // ignored silently til we hook a logger ¯\_(ツ)_/¯
        }

        super.processJarEntry(jarFile, zipfs, path, entries);
    }

    @Override
    protected void processJarEntry(JarFile jarFile, JarEntry entry, Set<String> entries) {
        String entryName = entry.getName();
        if (isFlatten()) {
            entryName = Paths.get(entryName).getFileName().toString();
        }

        String fileName = Path.of(jarFile.getName()).getFileName().toString();
        fileName = fileName.substring(0, fileName.length() - 4);
        Path destination = targetDirectory.resolve(fileName).resolve(entryName);

        try (InputStream in = jarFile.getInputStream(entry)) {
            Files.createDirectories(destination.getParent());
            Files.copy(in, destination, REPLACE_EXISTING);
        } catch (IOException e) {
            // ignored silently til we hook a logger ¯\_(ツ)_/¯
        }

        super.processJarEntry(jarFile, entry, entries);
    }
}
