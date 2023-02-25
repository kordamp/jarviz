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

import org.kordamp.jarviz.core.JarvizException;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class ModuleDescriptorJarPathAnalyzer implements JarPathAnalyzer<ModuleDescriptor> {
    private ModuleDescriptor moduleDescriptor;

    @Override
    public ModuleDescriptor getResult() {
        return moduleDescriptor;
    }

    @Override
    public void handle(Path path) throws JarvizException {
        try {
            moduleDescriptor = ModuleFinder.of(path).findAll().stream()
                .map(ModuleReference::descriptor)
                .findFirst()
                .get();
        } catch (RuntimeException e) {
            throw new JarvizException(e);
        }
    }
}
