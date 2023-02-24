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
package org.kordamp.jarviz.cli.services;

import org.kordamp.jarviz.cli.internal.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.processors.ServicesShowJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.util.Optional;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "show")
public class ServicesShow extends AbstractJarvizSubcommand<Services> {
    @CommandLine.Option(names = {"--service-name"}, required = true, paramLabel = "<name>")
    public String serviceName;

    @CommandLine.Option(names = {"--release"})
    public Integer file;

    @Override
    protected int execute() {
        JarFileResolver jarFileResolver = createJarFileResolver();
        ServicesShowJarProcessor processor = new ServicesShowJarProcessor(jarFileResolver);
        processor.setServiceName(serviceName);

        Set<JarProcessor.JarFileResult<Optional<Set<String>>>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(results);
        report(results);

        return 0;
    }

    private void output(Set<JarProcessor.JarFileResult<Optional<Set<String>>>> results) {
        Node root = createRootNode();
        for (JarProcessor.JarFileResult<Optional<Set<String>>> result : results) {
            if (null == outputFormat) {
                output(result);
            } else {
                buildReport(outputFormat, root, result);
            }
        }
        writeOutput(resolveFormatter(outputFormat).write(root));
    }

    private void output(JarProcessor.JarFileResult<Optional<Set<String>>> result) {
        if (result.getResult().isPresent()) {
            parent().getOut().println($$("output.subject", result.getJarFileName()));
            parent().getOut().println($$("services.show.service", serviceName));
            result.getResult().get().forEach(parent().getOut()::println);
        }
    }

    private void report(Set<JarProcessor.JarFileResult<Optional<Set<String>>>> results) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Optional<Set<String>>> result : results) {
                if (result.getResult().isPresent()) {
                    buildReport(format, root, result);
                }
            }
            if (null != outputFormat) writeReport(resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Format format, Node root, JarProcessor.JarFileResult<Optional<Set<String>>> result) {
        appendSubject(root, result.getJarPath(), "services list", resultNode -> {
            Node implementations = resultNode.node($("report.key.service")).value(serviceName).end()
                .array($("report.key.implementations"));

            for (String service : result.getResult().get()) {
                if (format != Format.XML) {
                    implementations.node(service);
                } else {
                    implementations.node($("report.key.implementation")).value(service).end();
                }
            }
        });
    }
}
