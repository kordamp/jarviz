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
package org.kordamp.jarviz.core.processors;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.model.Checksum;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.util.Algorithm;
import org.kordamp.jarviz.util.ChecksumUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toSet;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ChecksumJarProcessor implements JarProcessor<Set<Checksum>> {
    private static final String MAVEN_METADATA = "META-INF/maven/";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String VERSION = "version";
    private static final String GROUP_ID = "groupId";
    private final JarFileResolver jarFileResolver;

    public ChecksumJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    @Override
    public Set<JarFileResult<Set<Checksum>>> getResult() throws JarvizException {
        Set<JarFileResult<Set<Checksum>>> set = new TreeSet<>();

        Set<JarFile> jarFiles = jarFileResolver.resolveJarFiles();

        for (JarFile jarFile : jarFiles) {
            try (jarFile) {
                Set<JarEntry> candidates = jarFile.stream()
                    .filter(entry -> entry.getName().endsWith(".properties") && entry.getName().startsWith(MAVEN_METADATA))
                    .collect(toSet());

                findGAV(jarFile, candidates).ifPresent(props -> checksum(jarFile, props, set));
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_OPENING_JAR", jarFile.getName()));
            }
        }

        return set;
    }

    private Optional<Properties> findGAV(JarFile jarFile, Set<JarEntry> candidates) {
        String fileName = Path.of(jarFile.getName()).getFileName().toString();
        fileName = fileName.substring(0, fileName.length() - 4);

        for (JarEntry candidate : candidates) {
            try (InputStream in = jarFile.getInputStream(candidate)) {
                Properties props = new Properties();
                props.load(in);
                String artifactId = props.getProperty(ARTIFACT_ID);
                String version = props.getProperty(VERSION);
                if (fileName.startsWith(artifactId + "-" + version)) {
                    return Optional.of(props);
                }
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_OPENING_JAR", jarFile.getName()));
            }
        }

        return Optional.empty();
    }

    private void checksum(JarFile jarFile, Properties props, Set<JarFileResult<Set<Checksum>>> set) {
        String groupId = props.getProperty(GROUP_ID).replace(".", "/");
        String artifactId = props.getProperty(ARTIFACT_ID);
        String version = props.getProperty(VERSION);

        String filename = artifactId + "-" + version + ".jar";
        String baseUrl = "https://repo1.maven.org/maven2/" + groupId + "/" + artifactId + "/" + version + "/" + filename;

        Set<Checksum> checksums = new TreeSet<>();
        for (Algorithm algorithm : Algorithm.values()) {
            checksums.add(check(jarFile, algorithm, baseUrl));
        }

        set.add(JarFileResult.of(jarFile, checksums));
    }

    private Checksum check(JarFile jarFile, Algorithm algorithm, String baseUrl) {
        String str = baseUrl + algorithm.extension();

        URL url = null;
        try {
            url = new URI(str).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new JarvizException(RB.$("ERROR_INVALID_URL", str));
        }

        Path localJar = Path.of(jarFile.getName());
        Path remoteJar = null;

        try {
            remoteJar = Files.createTempFile(localJar.getFileName().toString(), algorithm.toString());
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_UNEXPECTED"), e);
        }

        try (InputStream stream = url.openStream()) {
            Files.copy(stream, remoteJar, REPLACE_EXISTING);
        } catch (FileNotFoundException ignored) {
            return Checksum.unavailable(algorithm);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_DOWNLOADING_URL", url), e);
        }

        try {
            String localChecksum = ChecksumUtils.checksum(algorithm, localJar);
            String remoteChecksum = new String(Files.readAllBytes(remoteJar));

            return localChecksum.equals(remoteChecksum) ? Checksum.success(algorithm) : Checksum.failure(algorithm);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_UNEXPECTED"), e);
        }
    }
}
