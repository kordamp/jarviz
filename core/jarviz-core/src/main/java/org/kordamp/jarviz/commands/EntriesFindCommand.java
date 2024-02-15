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
import org.kordamp.jarviz.core.processors.EntriesFindJarProcessor;
import org.kordamp.jarviz.core.processors.JarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.util.Set;

import static org.kordamp.jarviz.util.StringUtils.isBlank;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class EntriesFindCommand extends AbstractCommand<EntriesFindCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {
        private String entryName;
        private String entryPattern;

        public String getEntryName() {
            return entryName;
        }

        public Configuration withEntryName(String entryName) {
            this.entryName = entryName;
            if (isNotBlank(this.entryName)) {
                entryName = entryName.replaceAll("/", ".");
                if (entryName.startsWith(".")) entryName = entryName.substring(1);
            }
            return this;
        }

        public String getEntryPattern() {
            return entryPattern;
        }

        public Configuration withEntryPattern(String entryPattern) {
            this.entryPattern = entryPattern;
            return this;
        }
    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        EntriesFindJarProcessor processor = new EntriesFindJarProcessor(jarFileResolver);
        processor.setEntryName(configuration.getEntryName());
        processor.setEntryPattern(configuration.getEntryPattern());

        Set<JarProcessor.JarFileResult<Set<String>>> results = processor.getResult();
        // may have been updated
        configuration.withEntryPattern(processor.getEntryPattern());
        if (results.isEmpty()) {
            return 1;
        }

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
                buildReport(configuration, outputFormat, root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<Set<String>> result) {
        if (!result.getResult().isEmpty()) {
            configuration.getOut().println($$("output.subject", result.getJarFileName()));
            if (isBlank(configuration.getEntryPattern())) {
                configuration.getOut().println($$("entries.entry.name", configuration.getEntryName()));
            } else {
                configuration.getOut().println($$("entries.entry.pattern", configuration.getEntryPattern()));
            }
            result.getResult().forEach(configuration.getOut()::println);
        }
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<Set<String>>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Set<String>> result : results) {
                if (!result.getResult().isEmpty()) {
                    buildReport(configuration, format, root, result);
                }
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Configuration configuration, Format format, Node root, JarProcessor.JarFileResult<Set<String>> result) {
        appendSubject(root, result.getJarPath(), "entries find", resultNode -> {
            String key = isBlank(configuration.getEntryPattern()) ? "report.key.entry.name" : "report.key.entry.pattern";
            String value = isBlank(configuration.getEntryPattern()) ? configuration.getEntryName() : configuration.getEntryPattern();
            Node entries = resultNode.node(RB.$(key)).value(value).end()
                .array(RB.$("report.key.entries"));

            for (String service : result.getResult()) {
                if (format != Format.XML) {
                    entries.node(service);
                } else {
                    entries.node(RB.$("report.key.entry")).value(service).end();
                }
            }
        });
    }
}
