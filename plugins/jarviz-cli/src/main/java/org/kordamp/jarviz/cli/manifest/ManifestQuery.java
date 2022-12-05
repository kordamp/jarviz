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
package org.kordamp.jarviz.cli.manifest;

import org.kordamp.jarviz.cli.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.processors.QueryManifestJarProcessor;
import picocli.CommandLine;

import java.util.Optional;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "query")
public class ManifestQuery extends AbstractJarvizSubcommand<Manifest> {
    @CommandLine.Option(names = {"--attribute-name"}, required = true)
    public String attributeName;

    @CommandLine.Option(names = {"--section-name"})
    public String sectionName;

    @Override
    protected int execute() {
        QueryManifestJarProcessor processor = null != exclusive.file ?
            new QueryManifestJarProcessor(exclusive.file) :
            new QueryManifestJarProcessor(resolveOutputDirectory(), exclusive.url);
        processor.setAttributeName(attributeName);
        processor.setSectionName(sectionName);

        Optional<String> value = processor.getResult();
        if (value.isPresent()) {
            parent().getOut().println(value.get());
            return 0;
        }

        return 1;
    }
}
