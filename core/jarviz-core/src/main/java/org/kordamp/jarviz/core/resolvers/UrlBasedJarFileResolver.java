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
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarvizException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class UrlBasedJarFileResolver implements JarFileResolver {
    private final URL url;
    private final Path outputDirectory;

    public UrlBasedJarFileResolver(Path outputDirectory, URL url) {
        this.outputDirectory = outputDirectory;
        this.url = url;
    }

    @Override
    public JarFile resolveJarFile() {
        Path path = Paths.get(url.getPath()).getFileName();

        if (!path.getFileName().toString().endsWith(".jar")) {
            throw new JarvizException(RB.$("ERROR_PATH_IS_NOT_JAR", path.toAbsolutePath()));
        }

        Path file = outputDirectory.resolve(path);

        try (InputStream stream = url.openStream()) {
            Files.copy(stream, file, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_DOWNLOADING_URL", url), e);
        }

        try {
            return new JarFile(file.toFile());
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_OPENING_JAR", file.toAbsolutePath()));
        }
    }
}
