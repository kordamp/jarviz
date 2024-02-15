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
import org.kordamp.jarviz.core.processors.ServicesListJarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.util.Optional;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ServicesListCommand extends AbstractCommand<ServicesListCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {

    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        ServicesListJarProcessor processor = new ServicesListJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<Optional<Set<String>>>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(configuration, results);
        report(configuration, results);

        return 0;
    }

    private void output(Configuration configuration, Set<JarProcessor.JarFileResult<Optional<Set<String>>>> results) {
        Node root = createRootNode();
        Format outputFormat = configuration.getOutputFormat();
        for (JarProcessor.JarFileResult<Optional<Set<String>>> result : results) {
            if (null == outputFormat) {
                output(configuration, result);
            } else {
                buildReport(outputFormat, root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<Optional<Set<String>>> result) {
        if (result.getResult().isPresent()) {
            configuration.getOut().println($$("output.subject", result.getJarFileName()));
            result.getResult().get().forEach(configuration.getOut()::println);
        }
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<Optional<Set<String>>>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Optional<Set<String>>> result : results) {
                if (result.getResult().isPresent()) {
                    buildReport(format, root, result);
                }
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Format format, Node root, JarProcessor.JarFileResult<Optional<Set<String>>> result) {
        appendSubject(root, result.getJarPath(), "services list", resultNode -> {
            Node implementations = resultNode.array(RB.$("report.key.services"));

            for (String service : result.getResult().get()) {
                if (format != Format.XML) {
                    implementations.node(service);
                } else {
                    implementations.node(RB.$("report.key.service")).value(service).end();
                }
            }
        });
    }
}
