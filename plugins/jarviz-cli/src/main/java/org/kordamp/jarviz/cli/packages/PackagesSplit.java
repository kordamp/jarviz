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
package org.kordamp.jarviz.cli.packages;

import org.kordamp.jarviz.cli.internal.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.packages.SplitPackageJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
@CommandLine.Command(name = "split")
public class PackagesSplit extends AbstractJarvizSubcommand<Packages> {
    @Override
    protected int execute() {
        JarFileResolver jarFileResolver = createJarFileResolver();
        SplitPackageJarProcessor processor = new SplitPackageJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<Set<String>>> results = processor.getResult();
        for (JarProcessor.JarFileResult<Set<String>> result : results) {
            output(result);
            if (results.size() > 1) parent().getOut().println("");
        }
        report(results);

        return 0;
    }

    private void output(JarProcessor.JarFileResult<Set<String>> result) {
        parent().getOut().println($$("output.subject", result.getJarFileName()));
        result.getResult().forEach(parent().getOut()::println);
    }

    private void report(Set<JarProcessor.JarFileResult<Set<String>>> results) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Set<String>> result : results) {
                buildReport(format, root, result);
            }
            writeReport(resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Format format, Node root, JarProcessor.JarFileResult<Set<String>> result) {
        appendSubject(root, result.getJarPath(), "packages split", resultNode -> {
            Node packages = resultNode.array($("report.key.packages"));

            for (String thePackage : result.getResult()) {
                if (format != Format.XML) {
                    packages.node(thePackage);
                } else {
                    packages.node($("report.key.package")).value(thePackage).end();
                }
            }
        });
    }
}
