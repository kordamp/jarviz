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

import org.kordamp.jarviz.cli.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.modules.NameModuleJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.kordamp.jarviz.core.resolvers.JarFileResolvers.createJarFileResolver;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CommandLine.Command(name = "name")
public class ModuleName extends AbstractJarvizSubcommand<Module> {
    private static final String EXPLICIT = "explicit";
    private static final String FILENAME = "filename";
    private static final String MANIFEST = "manifest";

    @Override
    protected int execute() {
        JarFileResolver<?> jarFileResolver = createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveCacheDirectory());
        NameModuleJarProcessor processor = new NameModuleJarProcessor(jarFileResolver);

        org.kordamp.jarviz.core.model.ModuleName moduleName = processor.getResult();

        parent().getOut().println($$("module.name", moduleName.getModuleName()));
        parent().getOut().println($$("module.source", resolveSource(moduleName)));
        parent().getOut().println($$("module.automatic", !EXPLICIT.equals(resolveSource(moduleName))));
        parent().getOut().println($$("module.valid", $b(moduleName.isValid())));
        if (!moduleName.isValid()) {
            parent().getOut().println($$("module.reason", moduleName.getReason()));
        }

        report(jarFileResolver, moduleName);

        return 0;
    }

    private String resolveSource(org.kordamp.jarviz.core.model.ModuleName moduleName) {
        if (moduleName.isAutomaticByManifest()) return MANIFEST;
        if (moduleName.isAutomaticByFilename()) return FILENAME;
        return EXPLICIT;
    }

    private void report(JarFileResolver<?> jarFileResolver, org.kordamp.jarviz.core.model.ModuleName moduleName) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node node = buildReport(Paths.get(jarFileResolver.resolveJarFile().getName()), moduleName);
            writeReport(resolveFormatter(format).write(node), format);
        }
    }

    private Node buildReport(Path jarPath, org.kordamp.jarviz.core.model.ModuleName moduleName) {
        Node root = createRootNode(jarPath)
            .node($("report.key.name")).value(moduleName.getModuleName()).end()
            .node($("report.key.source")).value(resolveSource(moduleName)).end()
            .node($("report.key.automatic")).value(!EXPLICIT.equals(resolveSource(moduleName))).end()
            .node($("report.key.valid")).value(moduleName.isValid()).end();
        if (!moduleName.isValid()) {
            root.node($("report.key.reason")).value(moduleName.getReason()).end();
        }

        return root;
    }
}
