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

import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.resolvers.PathBasedJarFileResolver;
import org.kordamp.jarviz.core.resolvers.UrlBasedJarFileResolver;
import org.kordamp.jarviz.util.JarUtils;

import java.net.URL;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ShowManifestJarProcessor implements JarProcessor<Manifest> {
    private final JarFileResolver jarFileResolver;

    public ShowManifestJarProcessor(Path file) {
        this.jarFileResolver = new PathBasedJarFileResolver(file);
    }

    public ShowManifestJarProcessor(Path outputDirectory, URL url) {
        this.jarFileResolver = new UrlBasedJarFileResolver(outputDirectory, url);
    }

    @Override
    public Manifest getResult() {
        JarFile jarFile = jarFileResolver.resolveJarFile();
        return JarUtils.getManifest(jarFile);
    }
}
