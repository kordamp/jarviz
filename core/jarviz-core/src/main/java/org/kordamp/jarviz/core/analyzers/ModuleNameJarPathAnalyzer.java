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
package org.kordamp.jarviz.core.analyzers;

import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.model.ModuleName;

import java.lang.module.FindException;
import java.lang.module.InvalidModuleDescriptorException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.Optional;

import static org.kordamp.jarviz.util.StringUtils.isBlank;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class ModuleNameJarPathAnalyzer implements JarPathAnalyzer<ModuleName> {
    private final String automaticModuleNameByManifest;
    private final String automaticModuleNameByFilename;
    private ModuleName moduleName;

    public ModuleNameJarPathAnalyzer(String automaticModuleNameByManifest, String automaticModuleNameByFilename) {
        this.automaticModuleNameByManifest = automaticModuleNameByManifest;
        this.automaticModuleNameByFilename = automaticModuleNameByFilename;
    }

    @Override
    public ModuleName getResult() {
        return moduleName;
    }

    @Override
    public void handle(Path path) throws JarvizException {
        if (isNotBlank(automaticModuleNameByManifest)) {
            moduleName = ModuleName.fromAutomaticByManifest(automaticModuleNameByManifest,
                isAutomaticNameValid(automaticModuleNameByManifest).orElse(null));
        }

        try {
            moduleName = ModuleFinder.of(path).findAll().stream()
                .map(ModuleReference::descriptor)
                .map(this::toModuleName)
                .findFirst()
                .get();
        } catch (FindException fe) {
            Throwable cause = getRootCause(fe);
            if (cause instanceof InvalidModuleDescriptorException) {
                // explicit module descriptor is invalid
                // TODO: read module name from [versioned] module-info.class
                moduleName = ModuleName.fromModuleDescriptor("", cause.getMessage());
                return;
            }
            if (null == moduleName) {
                // automatic by filename
                moduleName = ModuleName.fromAutomaticByFilename(automaticModuleNameByFilename, cause.getMessage());
            }
        } catch (InvalidModuleDescriptorException imde) {
            // explicit
            // TODO: read module name from [versioned] module-info.class
            moduleName = ModuleName.fromModuleDescriptor("", imde.getMessage());
        }
    }

    private ModuleName toModuleName(ModuleDescriptor moduleDescriptor) {
        return ModuleName.fromModuleDescriptor(moduleDescriptor.name(),
            moduleDescriptor.isAutomatic() && isNotBlank(automaticModuleNameByManifest),
            moduleDescriptor.isAutomatic() && isBlank(automaticModuleNameByManifest) && isNotBlank(automaticModuleNameByFilename),
            isValid(moduleDescriptor.name()).orElse(null));
    }

    private Optional<String> isAutomaticNameValid(String name) {
        try {
            ModuleDescriptor.newAutomaticModule(name);
            return Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.of(exception.getMessage());
        }
    }

    private Optional<String> isValid(String name) {
        try {
            ModuleDescriptor.newModule(name);
            return Optional.empty();
        } catch (IllegalArgumentException exception) {
            return Optional.of(exception.getMessage());
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        while (null != throwable.getCause()) {
            throwable = throwable.getCause();
        }
        return throwable;
    }
}
