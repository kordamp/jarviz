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
import org.kordamp.jarviz.core.processors.QueryManifestJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.kordamp.jarviz.core.JarFileResolvers.createJarFileResolver;
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
        JarFileResolver<?> jarFileResolver = createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveCacheDirectory());
        QueryManifestJarProcessor processor = new QueryManifestJarProcessor(jarFileResolver);
        processor.setAttributeName(attributeName);
        processor.setSectionName(sectionName);

        Optional<String> value = processor.getResult();
        if (value.isPresent()) {
            parent().getOut().println(value.get());
            report(jarFileResolver, value.get());
            return 0;
        }

        return 1;
    }

    private void report(JarFileResolver<?> jarFileResolver, String value) {
        if (null == reportPath) return;

        Node node = buildReport(Paths.get(jarFileResolver.resolveJarFile().getName()), value);
        for (Format format : validateReportFormats()) {
            writeReport(resolveFormatter(format).write(node), format);
        }
    }

    private Node buildReport(Path jarPath, String value) {
        return appendSubject(createRootNode(), jarPath, "manifest query", resultNode -> {
            if (isNotBlank(sectionName)) {
                resultNode.node($("report.key.section.name")).value(sectionName).end();
            }

            resultNode.node($("report.key.attribute.name"))
                .node($("report.key.name")).value(attributeName).end()
                .node($("report.key.value")).value(value).end();
        });
    }
}
