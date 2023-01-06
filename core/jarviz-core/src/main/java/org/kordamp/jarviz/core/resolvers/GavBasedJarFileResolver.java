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
import java.net.MalformedURLException;
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
public class GavBasedJarFileResolver implements JarFileResolver {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final Path outputDirectory;

    public GavBasedJarFileResolver(Path outputDirectory, String gav) {
        this.outputDirectory = outputDirectory;

        String[] parts = gav.split(":");
        if (parts.length == 3) {
            this.groupId = parts[0].trim().replace(".", "/");
            this.artifactId = parts[1].trim();
            this.version = parts[2].trim();
        } else {
            throw new JarvizException(RB.$("ERROR_INVALID_GAV", gav));
        }
    }

    @Override
    public JarFile resolveJarFile() {
        String str = "https://repo1.maven.org/maven2/" + groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";

        URL url = null;
        try {
            url = new URL(str);
        } catch (MalformedURLException e) {
            throw new JarvizException(RB.$("ERROR_INVALID_URL", str));
        }

        Path path = Paths.get(url.getPath()).getFileName();
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
