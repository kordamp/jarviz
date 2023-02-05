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
package org.kordamp.jarviz.core.modules;

import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.analyzers.ModuleDescriptorJarPathAnalyzer;

import java.lang.module.ModuleDescriptor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class DescriptorModuleJarProcessor implements JarProcessor<ModuleDescriptor> {
    private final JarFileResolver jarFileResolver;

    public DescriptorModuleJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    @Override
    public ModuleDescriptor getResult() throws JarvizException {
        JarFile jarFile = jarFileResolver.resolveJarFile();
        Path jarPath = Paths.get(jarFile.getName());

        ModuleDescriptorJarPathAnalyzer analyzer = new ModuleDescriptorJarPathAnalyzer();
        analyzer.handle(jarPath);
        return analyzer.getResult();
    }
}
