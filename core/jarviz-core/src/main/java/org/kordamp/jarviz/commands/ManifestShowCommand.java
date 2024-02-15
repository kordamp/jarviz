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
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.internal.AbstractCommand;
import org.kordamp.jarviz.core.internal.AbstractConfiguration;
import org.kordamp.jarviz.core.processors.JarProcessor;
import org.kordamp.jarviz.core.processors.ManifestShowJarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ManifestShowCommand extends AbstractCommand<ManifestShowCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {

    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        ManifestShowJarProcessor processor = new ManifestShowJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(configuration, results);
        report(configuration, results);

        return 0;
    }

    private void output(Configuration configuration, Set<JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>>> results) {
        Node root = createRootNode();
        Format outputFormat = configuration.getOutputFormat();
        for (JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>> result : results) {
            if (null == outputFormat) {
                output(configuration, result);
            } else {
                buildReport(root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>> result) {
        if (result.getResult().isPresent()) {
            configuration.getOut().println($$("output.subject", result.getJarFileName()));
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                result.getResult().get().write(baos);
                baos.flush();
                baos.close();
                configuration.getOut().println(baos);
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_UNEXPECTED_WRITE"), e);
            }
        }
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>> result : results) {
                if (result.getResult().isPresent()) {
                    buildReport(root, result);
                }
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Node root, JarProcessor.JarFileResult<Optional<java.util.jar.Manifest>> result) {
        appendSubject(root, result.getJarPath(), "manifest show",
            resultNode -> resultNode.node(RB.$("report.key.manifest")).value(result.getResult().get()).end());
    }
}
