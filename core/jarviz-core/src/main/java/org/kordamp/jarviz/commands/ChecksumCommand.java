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
import org.kordamp.jarviz.core.model.Checksum;
import org.kordamp.jarviz.core.processors.ChecksumJarProcessor;
import org.kordamp.jarviz.core.processors.JarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.util.Set;

import static org.kordamp.jarviz.core.internal.Colorizer.cyan;
import static org.kordamp.jarviz.core.internal.Colorizer.green;
import static org.kordamp.jarviz.core.internal.Colorizer.red;
import static org.kordamp.jarviz.util.StringUtils.padRight;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ChecksumCommand extends AbstractCommand<ChecksumCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {

    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        ChecksumJarProcessor processor = new ChecksumJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<Set<Checksum>>> results = processor.getResult();

        output(configuration, results);
        report(configuration, results);

        return 0;
    }

    private void output(Configuration configuration, Set<JarProcessor.JarFileResult<Set<Checksum>>> results) {
        Node root = createRootNode();
        Format outputFormat = configuration.getOutputFormat();
        for (JarProcessor.JarFileResult<Set<Checksum>> result : results) {
            if (null == outputFormat) {
                output(configuration, result);
            } else {
                buildReport(root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<Set<Checksum>> result) {
        configuration.getOut().println($$("output.subject", result.getJarFileName()));
        result.getResult().forEach(checksum -> {
            String extension = padRight(checksum.getAlgorithm().extension(), 7);

            configuration.getOut().println(result.getJarFileName() + extension + " " + colorize(checksum.getOutcome()));
        });
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<Set<Checksum>>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Set<Checksum>> result : results) {
                buildReport(root, result);
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Node root, JarProcessor.JarFileResult<Set<Checksum>> result) {
        appendSubject(root, result.getJarPath(), "checksum", resultNode -> {
            Node checksums = resultNode.array(RB.$("report.key.checksums"));

            for (Checksum checksum : result.getResult()) {
                checksums.collapsable(RB.$("report.key.checksum"))
                    .node(RB.$("report.key.algorithm")).value(checksum.getAlgorithm().formatted()).end()
                    .node(RB.$("report.key.outcome")).value(checksum.getOutcome()).end();
            }
        });
    }

    private String colorize(Checksum.Outcome outcome) {
        switch (outcome) {
            case SUCCESS:
                return green(outcome.toString());
            case FAILURE:
                return red(outcome.toString());
            case UNAVAILABLE:
                return cyan(outcome.toString());
        }
        return "";
    }
}
