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
package org.kordamp.jarviz.cli;

import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractJarvizSubcommand<C extends IO> extends AbstractCommand<C> {
    @CommandLine.ArgGroup(multiplicity = "1")
    public Exclusive exclusive;

    public static class Exclusive {
        @CommandLine.Option(names = {"--file"})
        public Path file;

        @CommandLine.Option(names = {"--url"})
        public URL url;
    }

    @CommandLine.Option(names = {"--output-directory"})
    public Path outputdir;

    @CommandLine.ParentCommand
    public C parent;

    @Override
    protected C parent() {
        return parent;
    }

    protected int execute() {
        return 0;
    }
}
