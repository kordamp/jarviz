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
import org.kordamp.jarviz.core.internal.GavAwareJarFile;
import org.kordamp.jarviz.core.model.Gav;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.singleton;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GavBasedJarFileResolver implements JarFileResolver {
    private final Gav gav;
    private final Path cacheDirectory;
    private JarFile jarFile;

    public GavBasedJarFileResolver(Path cacheDirectory, String gav) {
        this.cacheDirectory = cacheDirectory;
        this.gav = new Gav(gav);
    }

    @Override
    public Set<JarFile> resolveJarFiles() {
        if (null != jarFile) return singleton(jarFile);

        String filename = gav.getArtifactId() + "-" + gav.getVersion() + (isNotBlank(gav.getClassifier()) ? "-" + gav.getClassifier() : "") + ".jar";
        String str = "https://repo1.maven.org/maven2/" + gav.getGroupId() + "/" + gav.getArtifactId() + "/" + gav.getVersion() + "/" + filename;
        String mavenLocal = String.join(File.separator, List.of(System.getProperty("user.home"), ".m2", "repository",
            gav.getGroupId().replace("/", File.separator), gav.getArtifactId(), gav.getVersion(), filename));

        URL url = null;
        try {
            url = new URI(str).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new JarvizException(RB.$("ERROR_INVALID_URL", str));
        }

        Instant remoteLastModified = null;
        try {
            remoteLastModified = Instant.ofEpochMilli(url.openConnection().getLastModified());
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_HEAD_URL", url));
        }

        // Naive check on local Maven repository
        Optional<Path> file = checkCachedFile(remoteLastModified, Path.of(mavenLocal));
        if (file.isEmpty()) {
            file = checkCachedFile(remoteLastModified, cacheDirectory.resolve(filename));
        }
        if (file.isPresent()) {
            return createJarFile(file.get());
        }

        Path path = cacheDirectory.resolve(filename);

        try (InputStream stream = url.openStream()) {
            Files.copy(stream, path, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_DOWNLOADING_URL", url), e);
        }

        return createJarFile(path);
    }

    private Optional<Path> checkCachedFile(Instant remoteLastModified, Path file) {
        if (Files.exists(file)) {
            try {
                Instant localLastModified = Files.getLastModifiedTime(file).toInstant();
                if (localLastModified.isAfter(remoteLastModified)) {
                    return Optional.of(file);
                }
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_FILE_LAST_MODIFIED", file.toAbsolutePath()));
            }
        }

        return Optional.empty();
    }

    private Set<JarFile> createJarFile(Path file) {
        try {
            jarFile = new GavAwareJarFile(file.toFile(), gav);
            return singleton(jarFile);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_OPENING_JAR", file.toAbsolutePath()));
        }
    }
}
