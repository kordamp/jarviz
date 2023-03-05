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
package org.kordamp.jarviz.cli.checksum;

import org.kordamp.jarviz.cli.Main;
import org.kordamp.jarviz.cli.internal.AbstractJarvizSubcommand;
import org.kordamp.jarviz.commands.ChecksumCommand;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
@CommandLine.Command(name = "checksum")
public class Checksum extends AbstractJarvizSubcommand<Main> {
    @CommandLine.Spec
    public CommandLine.Model.CommandSpec spec;

    @Override
    protected int execute() {
        return new ChecksumCommand().execute(ChecksumCommand.config()
            .withOut(parent().getOut())
            .withErr(parent().getErr())
            .withFailOnError(failOnError)
            .withGavs(collectEntries(gav))
            .withFiles(collectEntries(file))
            .withUrls(collectEntries(url))
            .withClasspaths(collectEntries(classpath))
            .withDirectories(collectEntries(directory))
            .withCacheDirectory(cache)
            .withReportPath(reportPath)
            .withReportFormats(resolveReportFormats())
            .withOutputFormat(outputFormat)
        );
    }
}
