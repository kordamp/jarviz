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
import org.kordamp.jarviz.core.internal.AbstractCommand;
import org.kordamp.jarviz.core.internal.AbstractConfiguration;
import org.kordamp.jarviz.core.processors.JarProcessor;
import org.kordamp.jarviz.core.processors.PackageSplitJarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class PackagesSplitCommand extends AbstractCommand<PackagesSplitCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {

    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        PackageSplitJarProcessor processor = new PackageSplitJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<Set<String>>> results = processor.getResult();

        output(configuration, results);
        report(configuration, results);

        return 0;
    }

    private void output(Configuration configuration, Set<JarProcessor.JarFileResult<Set<String>>> results) {
        Node root = createRootNode();
        Format outputFormat = configuration.getOutputFormat();
        for (JarProcessor.JarFileResult<Set<String>> result : results) {
            if (null == outputFormat) {
                output(configuration, result);
            } else {
                buildReport(outputFormat, root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<Set<String>> result) {
        configuration.getOut().println($$("output.subject", result.getJarFileName()));
        configuration.getOut().println($$("output.total", result.getResult().size()));
        result.getResult().forEach(configuration.getOut()::println);
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<Set<String>>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Set<String>> result : results) {
                buildReport(format, root, result);
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Format format, Node root, JarProcessor.JarFileResult<Set<String>> result) {
        appendSubject(root, result.getJarPath(), "packages split", resultNode -> {
            resultNode.node(RB.$("report.key.total")).value(result.getResult().size()).end();
            Node packages = resultNode.array(RB.$("report.key.packages"));

            for (String thePackage : result.getResult()) {
                if (format == Format.TXT) {
                    packages.node(thePackage);
                } else {
                    packages.collapsable(RB.$("report.key.package")).value(thePackage).end();
                }
            }
        });
    }
}
