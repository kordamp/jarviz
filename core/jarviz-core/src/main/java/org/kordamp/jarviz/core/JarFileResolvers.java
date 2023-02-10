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
package org.kordamp.jarviz.core;

import org.kordamp.jarviz.core.resolvers.GavBasedJarFileResolver;
import org.kordamp.jarviz.core.resolvers.PathBasedJarFileResolver;
import org.kordamp.jarviz.core.resolvers.UrlBasedJarFileResolver;

import java.net.URL;
import java.nio.file.Path;

import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JarFileResolvers {
    public static JarFileResolver<?> createJarFileResolver(Path file, String gav, URL url, Path cacheDirectory) {
        if (file != null) return new PathBasedJarFileResolver(file);
        if (isNotBlank(gav)) return new GavBasedJarFileResolver(cacheDirectory, gav);
        return new UrlBasedJarFileResolver(cacheDirectory, url);
    }
}