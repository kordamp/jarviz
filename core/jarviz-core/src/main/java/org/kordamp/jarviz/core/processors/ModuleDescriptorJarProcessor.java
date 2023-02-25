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
import org.kordamp.jarviz.core.analyzers.ModuleDescriptorJarPathAnalyzer;
import org.kordamp.jarviz.core.model.ModuleMetadata;
import org.kordamp.jarviz.core.model.ModuleName;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class ModuleDescriptorJarProcessor implements JarProcessor<ModuleMetadata> {
    private final JarFileResolver jarFileResolver;
    private final ModuleNameJarProcessor moduleNameJarProcessor;

    public ModuleDescriptorJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
        this.moduleNameJarProcessor = new ModuleNameJarProcessor(jarFileResolver);
    }

    @Override
    public Set<JarFileResult<ModuleMetadata>> getResult() throws JarvizException {
        Set<JarFileResult<ModuleMetadata>> set = new TreeSet<>();

        for (JarFile jarFile : jarFileResolver.resolveJarFiles()) {
            set.add(processJarFile(jarFile));
        }

        return set;
    }

    private JarFileResult<ModuleMetadata> processJarFile(JarFile jarFile) {
        JarFileResult<ModuleName> moduleName = moduleNameJarProcessor.processJarFile(jarFile);
        if (!moduleName.getResult().isValid()) {
            return JarFileResult.of(jarFile, ModuleMetadata.of(moduleName.getResult()));
        }

        Path jarPath = Paths.get(jarFile.getName());

        ModuleDescriptorJarPathAnalyzer analyzer = new ModuleDescriptorJarPathAnalyzer();
        analyzer.handle(jarPath);
        return JarFileResult.of(jarFile, ModuleMetadata.of(moduleName.getResult(), analyzer.getResult()));
    }
}
