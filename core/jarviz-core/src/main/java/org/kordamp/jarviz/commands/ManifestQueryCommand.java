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
package org.kordamp.jarviz.commands;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.Format;
import org.kordamp.jarviz.core.internal.AbstractCommand;
import org.kordamp.jarviz.core.internal.AbstractConfiguration;
import org.kordamp.jarviz.core.processors.JarProcessor;
import org.kordamp.jarviz.core.processors.ManifestQueryJarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.util.Optional;
import java.util.Set;

import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ManifestQueryCommand extends AbstractCommand<ManifestQueryCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {
        private String attributeName;
        private String sectionName;

        public String getAttributeName() {
            return attributeName;
        }

        public Configuration withAttributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public String getSectionName() {
            return sectionName;
        }

        public Configuration withSectionName(String sectionName) {
            this.sectionName = sectionName;
            return this;
        }
    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        ManifestQueryJarProcessor processor = new ManifestQueryJarProcessor(jarFileResolver);
        processor.setAttributeName(configuration.getAttributeName());
        processor.setSectionName(configuration.getSectionName());

        Set<JarProcessor.JarFileResult<Optional<String>>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(configuration, results);
        report(configuration, results);

        return 0;
    }

    private void output(Configuration configuration, Set<JarProcessor.JarFileResult<Optional<String>>> results) {
        Node root = createRootNode();
        Format outputFormat = configuration.getOutputFormat();
        for (JarProcessor.JarFileResult<Optional<String>> result : results) {
            if (null == outputFormat) {
                output(configuration, result);
            } else {
                buildReport(configuration, root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<Optional<String>> result) {
        if (result.getResult().isPresent()) {
            configuration.getOut().println($$("output.subject", result.getJarFileName()));
            configuration.getOut().println($$("manifest.query.attribute", configuration.getAttributeName(), result.getResult().get()));
        }
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<Optional<String>>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Optional<String>> result : results) {
                if (result.getResult().isPresent()) {
                    buildReport(configuration, root, result);
                }
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Configuration configuration, Node root, JarProcessor.JarFileResult<Optional<String>> result) {
        appendSubject(root, result.getJarPath(), "manifest query", resultNode -> {
            if (isNotBlank(configuration.getSectionName())) {
                resultNode.node(RB.$("report.key.section.name")).value(configuration.getSectionName()).end();
            }

            resultNode.node(RB.$("report.key.attribute.name"))
                .node(RB.$("report.key.name")).value(configuration.getAttributeName()).end()
                .node(RB.$("report.key.value")).value(result.getResult().get()).end();
        });
    }
}
