/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 The Jarviz authors.
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
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.resolvers.PathBasedJarFileResolver;
import org.kordamp.jarviz.core.resolvers.UrlBasedJarFileResolver;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class QueryManifestJarProcessor implements JarProcessor<Optional<String>> {
    private final JarFileResolver jarFileResolver;
    private String attributeName;
    private String sectionName;

    public QueryManifestJarProcessor(Path file) {
        this.jarFileResolver = new PathBasedJarFileResolver(file);
    }

    public QueryManifestJarProcessor(Path outputDirectory, URL url) {
        this.jarFileResolver = new UrlBasedJarFileResolver(outputDirectory, url);
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
    public Optional<String> getResult() {
        JarFile jarFile = jarFileResolver.resolveJarFile();

        Manifest manifest = null;
        try {
            manifest = jarFile.getManifest();
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_READING_JAR_MANIFEST", jarFile.getName()));
        }

        if (isNotBlank(sectionName)) {
            return Optional.ofNullable(manifest.getAttributes(sectionName)
                .getValue(attributeName));
        }

        return Optional.ofNullable(manifest.getMainAttributes().getValue(attributeName));
    }
}
