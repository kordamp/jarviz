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

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.cli.internal.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.processors.ShowManifestJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "show")
public class ManifestShow extends AbstractJarvizSubcommand<Manifest> {
    @Override
    protected int execute() {
        JarFileResolver jarFileResolver = createJarFileResolver();
        ShowManifestJarProcessor processor = new ShowManifestJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(results);
        report(results);

        return 0;
    }

    private void output(Set<JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>>> results) {
        Node root = createRootNode();
        for (JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>> result : results) {
            if (null == outputFormat) {
                output(result);
            } else {
                buildReport(root, result);
                writeOutput(resolveFormatter(outputFormat).write(root));
            }
            if (results.size() > 1) parent().getOut().println("");
        }
    }

    private void output(JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>> result) {
        if (result.getResult().isPresent()) {
            parent().getOut().println($$("output.subject", result.getJarFileName()));
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                result.getResult().get().write(baos);
                baos.flush();
                baos.close();
                parent().getOut().println(baos);
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_UNEXPECTED_WRITE"), e);
            }
        }
    }

    private void report(Set<JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>>> results) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>> result : results) {
                if (result.getResult().isPresent()) {
                    buildReport(root, result);
                }
            }
            writeReport(resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Node root, JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>> result) {
        appendSubject(root, result.getJarPath(), "manifest show",
            resultNode -> resultNode.node($("report.key.manifest")).value(result.getResult().get()).end());
    }
}
