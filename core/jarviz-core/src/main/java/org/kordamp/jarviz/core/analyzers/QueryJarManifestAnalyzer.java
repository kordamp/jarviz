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
package org.kordamp.jarviz.core.analyzers;

import org.kordamp.jarviz.core.JarManifestAnalyzer;
import org.kordamp.jarviz.core.JarvizException;

import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class QueryJarManifestAnalyzer implements JarManifestAnalyzer<Optional<String>> {
    private final String sectionName;
    private final String attributeName;
    private String result;

    public QueryJarManifestAnalyzer(String sectionName, String attributeName) {
        this.sectionName = sectionName;
        this.attributeName = attributeName;
    }

    public QueryJarManifestAnalyzer(String attributeName) {
        this(null, attributeName);
    }

    @Override
    public void handle(JarFile jarFile, Manifest manifest) throws JarvizException {
        if (null == manifest) return;

        if (isNotBlank(sectionName)) {
            Attributes attributes = manifest.getAttributes(sectionName);
            if (null != attributes) {
                result = attributes.getValue(attributeName);
            }
        } else {
            Attributes attributes = manifest.getMainAttributes();
            if (null != attributes) {
                result = attributes.getValue(attributeName);
            }
        }
    }

    @Override
    public Optional<String> getResult() {
        return Optional.ofNullable(result);
    }
}
