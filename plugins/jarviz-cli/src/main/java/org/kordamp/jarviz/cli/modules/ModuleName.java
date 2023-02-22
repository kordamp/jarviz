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

import org.kordamp.jarviz.cli.internal.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.modules.NameModuleJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.util.Set;

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
        JarFileResolver jarFileResolver = createJarFileResolver();
        NameModuleJarProcessor processor = new NameModuleJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(results);
        report(results);

        return 0;
    }

    private void output(Set<JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName>> results) {
        Node root = createRootNode();
        for (JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName> result : results) {
            if (null == outputFormat) {
                output(result);
            } else {
                buildReport(root, result);
            }
        }
        if (null != outputFormat) writeOutput(resolveFormatter(outputFormat).write(root));
    }

    private void output(JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName> result) {
        parent().getOut().println($$("output.subject", result.getJarFileName()));
        org.kordamp.jarviz.core.model.ModuleName moduleName = result.getResult();

        parent().getOut().println($$("module.name", moduleName.getModuleName()));
        parent().getOut().println($$("module.source", resolveSource(moduleName)));
        parent().getOut().println($$("module.automatic", $b(!EXPLICIT.equals(resolveSource(moduleName)))));
        parent().getOut().println($$("module.valid", $b(moduleName.isValid())));
        if (!moduleName.isValid()) {
            parent().getOut().println($$("module.reason", moduleName.getReason()));
        }
    }

    private String resolveSource(org.kordamp.jarviz.core.model.ModuleName moduleName) {
        if (moduleName.isAutomaticByManifest()) return MANIFEST;
        if (moduleName.isAutomaticByFilename()) return FILENAME;
        return EXPLICIT;
    }

    private void report(Set<JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName>> results) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName> result : results) {
                buildReport(root, result);
            }
            writeReport(resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Node root, JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName> result) {
        org.kordamp.jarviz.core.model.ModuleName moduleName = result.getResult();

        appendSubject(root, result.getJarPath(), "module name", resultNode -> {
            resultNode.node($("report.key.name")).value(moduleName.getModuleName()).end()
                .node($("report.key.source")).value(resolveSource(moduleName)).end()
                .node($("report.key.automatic")).value(!EXPLICIT.equals(resolveSource(moduleName))).end()
                .node($("report.key.valid")).value(moduleName.isValid()).end();
            if (!moduleName.isValid()) {
                resultNode.node($("report.key.reason")).value(moduleName.getReason()).end();
            }
        });
    }
}
