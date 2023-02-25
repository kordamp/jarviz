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
package org.kordamp.jarviz.core.resolvers;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class CompositeJarFileResolver implements JarFileResolver {
    private final Set<JarFileResolver> resolvers = new LinkedHashSet<>();

    public CompositeJarFileResolver(Set<JarFileResolver> resolvers) {
        this.resolvers.addAll(resolvers);
    }

    @Override
    public Set<JarFile> resolveJarFiles() {
        Set<JarFile> jarFiles = new LinkedHashSet<>();

        for (JarFileResolver resolver : resolvers) {
            jarFiles.addAll(resolver.resolveJarFiles());
        }

        return jarFiles;
    }
}
