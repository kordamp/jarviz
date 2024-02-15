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
package org.kordamp.jarviz.core.model;

import java.lang.module.ModuleDescriptor;
import java.util.Optional;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ModuleMetadata {
    private final ModuleName moduleName;
    private final ModuleDescriptor moduleDescriptor;

    public static ModuleMetadata of(ModuleName moduleName) {
        return new ModuleMetadata(moduleName, null);
    }

    public static ModuleMetadata of(ModuleName moduleName, ModuleDescriptor moduleDescriptor) {
        return new ModuleMetadata(moduleName, moduleDescriptor);
    }

    private ModuleMetadata(ModuleName moduleName, ModuleDescriptor moduleDescriptor) {
        this.moduleName = moduleName;
        this.moduleDescriptor = moduleDescriptor;
    }

    public ModuleName getModuleName() {
        return moduleName;
    }

    public Optional<ModuleDescriptor> getModuleDescriptor() {
        return Optional.ofNullable(moduleDescriptor);
    }
}
