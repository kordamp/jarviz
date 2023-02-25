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

import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.analyzers.ModuleNameJarPathAnalyzer;
import org.kordamp.jarviz.core.analyzers.QueryJarManifestAnalyzer;
import org.kordamp.jarviz.core.model.ModuleName;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.util.JarUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.kordamp.jarviz.core.internal.Constants.ATTR_AUTOMATIC_MODULE_NAME;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class ModuleNameJarProcessor implements JarProcessor<ModuleName> {
    // This should be the correct pattern
    // private static final Pattern VERSION_PATTERN = Pattern.compile("-(\\d+(\\.|_|-|\\+|$))");
    private static final Pattern VERSION_PATTERN = Pattern.compile("-(\\d+(\\.|$))");
    private final JarFileResolver jarFileResolver;

    public ModuleNameJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    @Override
    public Set<JarFileResult<ModuleName>> getResult() throws JarvizException {
        Set<JarFileResult<ModuleName>> set = new TreeSet<>();

        for (JarFile jarFile : jarFileResolver.resolveJarFiles()) {
            set.add(processJarFile(jarFile));
        }

        return set;
    }

    JarFileResult<ModuleName> processJarFile(JarFile jarFile) {
        Path jarPath = Paths.get(jarFile.getName());
        String automaticModuleNameByFilename = deriveModuleNameFromFilename(jarPath.getFileName().toString());
        String automaticModuleNameByManifest = null;

        Optional<Manifest> manifest = JarUtils.getManifest(jarFile);
        if (manifest.isPresent()) {
            QueryJarManifestAnalyzer analyzer = new QueryJarManifestAnalyzer(ATTR_AUTOMATIC_MODULE_NAME);
            analyzer.handle(jarFile, manifest.get());
            automaticModuleNameByManifest = analyzer.getResult().orElse(null);
        }

        ModuleNameJarPathAnalyzer analyzer = new ModuleNameJarPathAnalyzer(automaticModuleNameByManifest, automaticModuleNameByFilename);
        analyzer.handle(jarPath);
        return JarFileResult.of(jarFile, analyzer.getResult());
    }

    private String deriveModuleNameFromFilename(String filename) {
        String name = filename.substring(0, filename.length() - 4);

        Matcher matcher = VERSION_PATTERN.matcher(name);
        if (matcher.find()) {
            name = name.substring(0, matcher.start());
        }

        return name.replaceAll("[^A-Za-z0-9]", ".");
    }
}
