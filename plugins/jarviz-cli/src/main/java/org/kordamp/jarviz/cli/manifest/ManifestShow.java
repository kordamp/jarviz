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
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.processors.ShowManifestJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.kordamp.jarviz.core.JarFileResolvers.createJarFileResolver;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "show")
public class ManifestShow extends AbstractJarvizSubcommand<Manifest> {
    @Override
    protected int execute() {
        JarFileResolver<?> jarFileResolver = createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveCacheDirectory());
        ShowManifestJarProcessor processor = new ShowManifestJarProcessor(jarFileResolver);

        Optional<java.util.jar.Manifest> manifest = processor.getResult();
        if (manifest.isEmpty()) return 1;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            manifest.get().write(baos);
            baos.flush();
            baos.close();
            parent().getOut().println(baos);
            report(jarFileResolver, baos.toString().trim());
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_UNEXPECTED_WRITE"), e);
        }

        return 0;
    }

    private void report(JarFileResolver<?> jarFileResolver, String manifest) {
        if (null == reportPath) return;

        Node node = buildReport(Paths.get(jarFileResolver.resolveJarFile().getName()), manifest);
        for (Format format : validateReportFormats()) {
            writeReport(resolveFormatter(format).write(node), format);
        }
    }

    private Node buildReport(Path jarPath, String manifest) {
        return createRootNode(jarPath)
            .node($("report.key.manifest")).value(manifest).end();
    }
}
