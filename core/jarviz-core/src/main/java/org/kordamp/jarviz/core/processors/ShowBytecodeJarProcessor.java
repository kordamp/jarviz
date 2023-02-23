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

import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.analyzers.QueryJarManifestAnalyzer;
import org.kordamp.jarviz.core.model.BytecodeVersion;
import org.kordamp.jarviz.core.model.BytecodeVersions;
import org.kordamp.jarviz.util.JarUtils;

import java.util.Enumeration;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static org.kordamp.jarviz.core.Constants.ATTR_BYTECODE_VERSION;
import static org.kordamp.jarviz.util.JarUtils.readBytecodeVersion;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ShowBytecodeJarProcessor implements JarProcessor<BytecodeVersions> {
    private static final Pattern MULTIRELEASE = Pattern.compile("META-INF/versions/(\\d+)/(.*\\.class)");

    private final JarFileResolver jarFileResolver;

    public ShowBytecodeJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    @Override
    public Set<JarFileResult<BytecodeVersions>> getResult() throws JarvizException {
        Set<JarFileResult<BytecodeVersions>> set = new TreeSet<>();

        for (JarFile jarFile : jarFileResolver.resolveJarFiles()) {
            set.add(processJarFile(jarFile));
        }

        return set;
    }

    private JarFileResult<BytecodeVersions> processJarFile(JarFile jarFile) {
        BytecodeVersions bytecodeVersions = new BytecodeVersions();
        Optional<Manifest> manifest = JarUtils.getManifest(jarFile);

        if (manifest.isPresent()) {
            QueryJarManifestAnalyzer analyzer = new QueryJarManifestAnalyzer(ATTR_BYTECODE_VERSION);
            analyzer.handle(jarFile, manifest.get());
            analyzer.getResult().ifPresent(v -> {
                Set<BytecodeVersion> set = new TreeSet<>();
                stream(v.split(","))
                    .map(Integer::parseInt)
                    .map(BytecodeVersion::of)
                    .forEach(set::add);
                bytecodeVersions.setManifestBytecode(set);
            });
        }

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entryName.endsWith(".class")) continue;

            BytecodeVersion bytecodeVersion = readBytecodeVersion(jarFile, entry);
            Matcher matcher = MULTIRELEASE.matcher(entryName);
            if (matcher.matches()) {
                // TODO: Report only if JAR is multi-release?
                if (jarFile.isMultiRelease()) {
                    Integer javaVersion = Integer.parseInt(matcher.group(1));
                    String className = matcher.group(2)
                        .replace('/', '.')
                        .replace('\\', '.')
                        .replace('$', '.');
                    className = className.substring(0, className.length() - 6);
                    bytecodeVersions.addVersionedClass(javaVersion, bytecodeVersion, className);
                }
            } else {
                String className = entryName
                    .replace('/', '.')
                    .replace('\\', '.')
                    .replace('$', '.');
                className = className.substring(0, className.length() - 6);
                bytecodeVersions.addUnversionedClass(bytecodeVersion, className);
            }
        }

        return JarFileResult.of(jarFile, bytecodeVersions);
    }
}
