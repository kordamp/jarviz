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
package org.kordamp.jarviz.commands;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.Format;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.internal.AbstractCommand;
import org.kordamp.jarviz.core.internal.AbstractConfiguration;
import org.kordamp.jarviz.core.model.ModuleName;
import org.kordamp.jarviz.core.processors.JarProcessor;
import org.kordamp.jarviz.core.processors.ModuleNameJarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.util.Set;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toSet;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ModuleNameCommand extends AbstractCommand<ModuleNameCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {

    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        ModuleNameJarProcessor processor = new ModuleNameJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(configuration, results);
        report(configuration, results);

        Set<String> errors = results.stream()
            .map(JarProcessor.JarFileResult::getResult)
            .filter(ModuleName::isNotValid)
            .map(ModuleName::asError)
            .collect(toSet());

        if (!errors.isEmpty()) {
            if (configuration.isFailOnError()) {
                throw new JarvizException(String.join(lineSeparator(), errors));
            } else {
                configuration.getErr().println(String.join(lineSeparator(), errors));
            }
        }

        return 0;
    }

    private void output(Configuration configuration, Set<JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName>> results) {
        Node root = createRootNode();
        Format outputFormat = configuration.getOutputFormat();
        for (JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName> result : results) {
            if (null == outputFormat) {
                output(configuration, result);
            } else {
                buildReport(root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName> result) {
        configuration.getOut().println($$("output.subject", result.getJarFileName()));
        org.kordamp.jarviz.core.model.ModuleName moduleName = result.getResult();

        configuration.getOut().println($$("module.name", moduleName.getModuleName()));
        configuration.getOut().println($$("module.source", moduleName.resolveSource()));
        configuration.getOut().println($$("module.automatic", $b(moduleName.isAutomatic())));
        configuration.getOut().println($$("module.valid", $b(moduleName.isValid())));
        if (!moduleName.isValid()) {
            configuration.getOut().println($$("module.reason", moduleName.getReason()));
        }
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName> result : results) {
                buildReport(root, result);
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Node root, JarProcessor.JarFileResult<org.kordamp.jarviz.core.model.ModuleName> result) {
        org.kordamp.jarviz.core.model.ModuleName moduleName = result.getResult();

        appendSubject(root, result.getJarPath(), "module name", resultNode -> {
            resultNode.node(RB.$("report.key.name")).value(moduleName.getModuleName()).end()
                .node(RB.$("report.key.source")).value(moduleName.resolveSource()).end()
                .node(RB.$("report.key.automatic")).value(moduleName.isAutomatic()).end()
                .node(RB.$("report.key.valid")).value(moduleName.isValid()).end();
            if (!moduleName.isValid()) {
                resultNode.node(RB.$("report.key.reason")).value(moduleName.getReason()).end();
            }
        });
    }
}
