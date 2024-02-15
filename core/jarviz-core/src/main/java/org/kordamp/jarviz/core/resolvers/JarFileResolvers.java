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
package org.kordamp.jarviz.core.resolvers;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JarFileResolvers {
    public static JarFileResolver createJarFileResolver(Path file, String gav, URL url, Path cacheDirectory) {
        if (file != null) return new PathBasedJarFileResolver(file);
        if (isNotBlank(gav)) return new GavBasedJarFileResolver(cacheDirectory, gav);
        return new UrlBasedJarFileResolver(cacheDirectory, url);
    }

    public static Set<JarFileResolver> classpathJarFileResolvers(Collection<String> classpaths) {
        if (classpaths.isEmpty()) return emptySet();
        return classpaths.stream()
            .map(ClasspathBasedJarFileResolver::new)
            .collect(toSet());
    }

    public static Set<JarFileResolver> directoryJarFileResolvers(Collection<Path> directories) {
        if (directories.isEmpty()) return emptySet();
        return directories.stream()
            .map(DirectoryBasedJarFileResolver::new)
            .collect(toSet());
    }

    public static Set<JarFileResolver> pathJarFileResolvers(Collection<Path> files) {
        if (files.isEmpty()) return emptySet();
        return files.stream()
            .map(PathBasedJarFileResolver::new)
            .collect(toSet());
    }

    public static Set<JarFileResolver> gavJarFileResolvers(Path cacheDirectory, Collection<String> gavs) {
        if (gavs.isEmpty()) return emptySet();
        return gavs.stream()
            .map(gav -> new GavBasedJarFileResolver(cacheDirectory, gav))
            .collect(toSet());
    }

    public static Set<JarFileResolver> urlJarFileResolvers(Path cacheDirectory, Collection<URL> urls) {
        if (urls.isEmpty()) return emptySet();
        return urls.stream()
            .map(url -> new UrlBasedJarFileResolver(cacheDirectory, url))
            .collect(toSet());
    }

    public static JarFileResolver compositeJarFileResolver(Set<JarFileResolver> resolvers) {
        return new CompositeJarFileResolver(resolvers);
    }
}