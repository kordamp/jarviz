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
package org.kordamp.jarviz.cli.manifest;

import org.kordamp.jarviz.cli.internal.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.processors.QueryManifestJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.util.Optional;
import java.util.Set;

import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "query")
public class ManifestQuery extends AbstractJarvizSubcommand<Manifest> {
    @CommandLine.Option(names = {"--attribute-name"}, required = true, paramLabel = "<name>")
    public String attributeName;

    @CommandLine.Option(names = {"--section-name"}, paramLabel = "<name>")
    public String sectionName;

    @Override
    protected int execute() {
        JarFileResolver jarFileResolver = createJarFileResolver();
        QueryManifestJarProcessor processor = new QueryManifestJarProcessor(jarFileResolver);
        processor.setAttributeName(attributeName);
        processor.setSectionName(sectionName);

        Set<JarProcessor.JarFileResult<Optional<String>>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(results);
        report(results);

        return 0;
    }

    private void output(Set<JarProcessor.JarFileResult<Optional<String>>> results) {
        Node root = createRootNode();
        for (JarProcessor.JarFileResult<Optional<String>> result : results) {
            if (null == outputFormat) {
                output(result);
            } else {
                buildReport(root, result);
                writeOutput(resolveFormatter(outputFormat).write(root));
            }
            if (results.size() > 1) parent().getOut().println("");
        }
    }

    private void output(JarProcessor.JarFileResult<Optional<String>> result) {
        if (result.getResult().isPresent()) {
            parent().getOut().println($$("output.subject", result.getJarFileName()));
            parent().getOut().println($$("manifest.query.attribute", attributeName, result.getResult().get()));
        }
    }

    private void report(Set<JarProcessor.JarFileResult<Optional<String>>> results) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Optional<String>> result : results) {
                if (result.getResult().isPresent()) {
                    buildReport(root, result);
                }
            }
            writeReport(resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Node root, JarProcessor.JarFileResult<Optional<String>> result) {
        appendSubject(root, result.getJarPath(), "manifest query", resultNode -> {
            if (isNotBlank(sectionName)) {
                resultNode.node($("report.key.section.name")).value(sectionName).end();
            }

            resultNode.node($("report.key.attribute.name"))
                .node($("report.key.name")).value(attributeName).end()
                .node($("report.key.value")).value(result.getResult().get()).end();
        });
    }
}
