/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 The Jarviz authors.
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
import org.kordamp.jarviz.core.services.ShowServicesJarProcessor;
import picocli.CommandLine;

import java.util.List;
import java.util.Optional;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "show")
public class ServicesShow extends AbstractJarvizSubcommand<Services> {
    @CommandLine.Option(names = {"--service-name"}, required = true)
    public String serviceName;

    @CommandLine.Option(names = {"--release"})
    public Integer file;

    @Override
    protected int execute() {
        ShowServicesJarProcessor processor = null != exclusive.file ?
            new ShowServicesJarProcessor(exclusive.file) :
            new ShowServicesJarProcessor(resolveOutputDirectory(), exclusive.url);
        processor.setServiceName(serviceName);

        Optional<List<String>> services = processor.getResult();
        if (services.isPresent()) {
            services.get().forEach(parent().getOut()::println);
            return 0;
        }

        return 1;
    }
}
