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

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.model.Checksum;
import org.kordamp.jarviz.core.model.Gav;
import org.kordamp.jarviz.core.model.GavAware;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toSet;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ChecksumJarProcessor implements JarProcessor<Set<Checksum>> {
    private static final Pattern CHECKSUM = Pattern.compile("^([a-fA-F0-9]+).*$");

    private static final String MAVEN_METADATA = "META-INF/maven/";
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
                if (jarFile instanceof GavAware) {
                    checksum(jarFile, ((GavAware) jarFile).getGav(), set);
                } else {
                    Set<JarEntry> candidates = jarFile.stream()
                        .filter(entry -> entry.getName().endsWith(".properties") && entry.getName().startsWith(MAVEN_METADATA))
                        .collect(toSet());

                    findGav(jarFile, candidates).ifPresent(gav -> checksum(jarFile, gav, set));
                }
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_OPENING_JAR", jarFile.getName()));
            }
        }

        return set;
    }

    private Optional<Gav> findGav(JarFile jarFile, Set<JarEntry> candidates) {
        String fileName = Path.of(jarFile.getName()).getFileName().toString();
        fileName = fileName.substring(0, fileName.length() - 4);

        for (JarEntry candidate : candidates) {
            try (InputStream in = jarFile.getInputStream(candidate)) {
                Properties props = new Properties();
                props.load(in);
                String artifactId = props.getProperty(Gav.ARTIFACT_ID);
                String version = props.getProperty(Gav.VERSION);
                if (fileName.startsWith(artifactId + "-" + version)) {
                    return Optional.of(new Gav(props));
                }
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_OPENING_JAR", jarFile.getName()));
            }
        }

        return Optional.empty();
    }

    private void checksum(JarFile jarFile, Gav gav, Set<JarFileResult<Set<Checksum>>> set) {
        String groupId = gav.getGroupId().replace(".", "/");
        String artifactId = gav.getArtifactId();
        String version = gav.getVersion();

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
            String remoteChecksum = sanitize(new String(Files.readAllBytes(remoteJar)).trim());

            return localChecksum.equals(remoteChecksum) ? Checksum.success(algorithm) : Checksum.failure(algorithm);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_UNEXPECTED"), e);
        }
    }

    private String sanitize(String input) {
        // some checksums available ay Maven Central have this format
        // 28e7eb9eeefe31a657c68755bfccc541  -
        Matcher matcher = CHECKSUM.matcher(input);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }
}
