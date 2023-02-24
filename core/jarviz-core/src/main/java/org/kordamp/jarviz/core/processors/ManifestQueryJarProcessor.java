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
import org.kordamp.jarviz.util.JarUtils;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ManifestQueryJarProcessor implements JarProcessor<Optional<String>> {
    private final JarFileResolver jarFileResolver;
    private String attributeName;
    private String sectionName;

    public ManifestQueryJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    @Override
    public Set<JarFileResult<Optional<String>>> getResult() throws JarvizException {
        Set<JarFileResult<Optional<String>>> set = new TreeSet<>();

        for (JarFile jarFile : jarFileResolver.resolveJarFiles()) {
            set.add(processJarfile(jarFile));
        }

        return set;
    }

    private JarFileResult<Optional<String>> processJarfile(JarFile jarFile) {
        Optional<Manifest> manifest = JarUtils.getManifest(jarFile);

        if (manifest.isPresent()) {
            QueryJarManifestAnalyzer analyzer = new QueryJarManifestAnalyzer(sectionName, attributeName);
            analyzer.handle(jarFile, manifest.get());
            return JarFileResult.of(jarFile, analyzer.getResult());
        }
        return JarFileResult.of(jarFile, Optional.empty());
    }
}
