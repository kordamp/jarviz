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
package org.kordamp.jarviz.cli.modules;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.cli.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.modules.NameModuleJarProcessor;
import picocli.CommandLine;

import static org.kordamp.jarviz.core.resolvers.JarFileResolvers.createJarFileResolver;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CommandLine.Command(name = "name")
public class ModuleName extends AbstractJarvizSubcommand<Module> {
    @Override
    protected int execute() {
        NameModuleJarProcessor processor = new NameModuleJarProcessor(createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveOutputDirectory()));

        org.kordamp.jarviz.core.model.ModuleName moduleName = processor.getResult();

        parent().getOut().println(RB.$("module.name.name", moduleName.getModuleName()));
        parent().getOut().println(RB.$("module.name.automatic", resolveAutomatic(moduleName)));
        parent().getOut().println(RB.$("module.name.valid", moduleName.isValid()));
        if (!moduleName.isValid()) {
            parent().getOut().println(RB.$("module.name.reason", moduleName.getReason()));
        }

        return 0;
    }

    private String resolveAutomatic(org.kordamp.jarviz.core.model.ModuleName moduleName) {
        if (moduleName.isAutomaticByManifest()) return "manifest";
        if (moduleName.isAutomaticByFilename()) return "filename";
        return "no";
    }
}
