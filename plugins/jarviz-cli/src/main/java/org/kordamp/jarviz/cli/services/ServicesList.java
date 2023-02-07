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

import org.kordamp.jarviz.cli.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.services.ListServicesJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.kordamp.jarviz.core.resolvers.JarFileResolvers.createJarFileResolver;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "list")
public class ServicesList extends AbstractJarvizSubcommand<Services> {
    // @CommandLine.Option(names = {"--release"})
    // public Integer file;

    @Override
    protected int execute() {
        JarFileResolver<?> jarFileResolver = createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveCacheDirectory());
        ListServicesJarProcessor processor = new ListServicesJarProcessor(jarFileResolver);

        Optional<List<String>> services = processor.getResult();
        if (services.isPresent()) {
            services.get().forEach(parent().getOut()::println);
            report(jarFileResolver, services.get());
            return 0;
        }

        return 1;
    }

    private void report(JarFileResolver<?> jarFileResolver, List<String> services) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node node = buildReport(format, Paths.get(jarFileResolver.resolveJarFile().getName()), services);
            writeReport(resolveFormatter(format).write(node), format);
        }
    }

    private Node buildReport(Format format, Path jarPath, List<String> services) {
        Node root = createRootNode(jarPath);
        Node implementations = root.array($("report.key.services"));

        for (String service : services) {
            if (format != Format.XML) {
                implementations.node(service);
            } else {
                implementations.node($("report.key.service")).value(service).end();
            }
        }
        return root;
    }
}
