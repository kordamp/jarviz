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
package org.kordamp.jarviz.cli.internal;

import org.kordamp.jarviz.cli.IO;
import org.kordamp.jarviz.core.Format;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractJarvizSubcommand<C extends IO> extends AbstractCommand<C> {
    @CommandLine.Option(names = {"--fail-on-error"},
        negatable = true,
        defaultValue = "true", fallbackValue = "true")
    public boolean failOnError;

    @CommandLine.Option(names = {"--directory"})
    public Path[] directory;

    @CommandLine.Option(names = {"--file"})
    public Path[] file;

    @CommandLine.Option(names = {"--gav"})
    public String[] gav;

    @CommandLine.Option(names = {"--url"})
    public URL[] url;

    @CommandLine.Option(names = {"--classpath"})
    public String[] classpath;

    @CommandLine.Option(names = {"--cache-directory"}, paramLabel = "<directory>")
    public Path cache;

    @CommandLine.ParentCommand
    public C parent;

    @CommandLine.Option(names = {"--report-path"}, paramLabel = "<path>")
    protected Path reportPath;

    @CommandLine.Option(names = {"--report-format"}, paramLabel = "<format>")
    Format[] reportFormats;

    @CommandLine.Option(names = {"--output-format"}, paramLabel = "<format>")
    protected Format outputFormat;

    @Override
    protected C parent() {
        return parent;
    }

    protected int execute() {
        return 0;
    }

    protected Set<Format> resolveReportFormats() {
        if (null != reportPath && (null == reportFormats || reportFormats.length == 0)) {
            return singleton(Format.TXT);
        }

        return null == reportFormats ? emptySet() : Arrays.stream(reportFormats)
            .collect(toSet());
    }

    protected Set<Path> collectEntries(Path[] input) {
        Set<Path> set = new TreeSet<>();
        if (null != input) {
            Collections.addAll(set, input);
        }
        return set;
    }

    protected Set<URL> collectEntries(URL[] input) {
        Set<URL> set = new LinkedHashSet<>();
        if (null != input) {
            Collections.addAll(set, input);
        }
        return set;
    }

    protected Set<String> collectEntries(String[] input) {
        Set<String> set = new LinkedHashSet<>();
        if (null != input) {
            for (String s : input) {
                if (isNotBlank(s)) {
                    set.add(s.trim());
                }
            }
        }
        return set;
    }
}
