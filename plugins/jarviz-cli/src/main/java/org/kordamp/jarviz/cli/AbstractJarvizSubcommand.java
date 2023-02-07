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
package org.kordamp.jarviz.cli;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Formatter;
import org.kordamp.jarviz.reporting.JsonFormatter;
import org.kordamp.jarviz.reporting.Node;
import org.kordamp.jarviz.reporting.TxtFormatter;
import org.kordamp.jarviz.reporting.XmlFormatter;
import org.kordamp.jarviz.reporting.YamlFormatter;
import org.kordamp.jarviz.util.ChecksumUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.toList;
import static org.kordamp.jarviz.cli.internal.Colorizer.bool;
import static org.kordamp.jarviz.cli.internal.Colorizer.colorize;
import static org.kordamp.jarviz.util.StringUtils.getHyphenatedName;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractJarvizSubcommand<C extends IO> extends AbstractCommand<C> {
    public static final String INDENT = "  ";
    public static final String SPACE = " ";
    public static final String EMPTY = "";

    @CommandLine.ArgGroup(multiplicity = "1")
    public Exclusive exclusive;

    public static class Exclusive {
        @CommandLine.Option(names = {"--file"})
        public Path file;

        @CommandLine.Option(names = {"--gav"})
        public String gav;

        @CommandLine.Option(names = {"--url"})
        public URL url;
    }

    @CommandLine.Option(names = {"--cache-directory"}, paramLabel = "<directory>")
    public Path cache;

    @CommandLine.ParentCommand
    public C parent;

    @CommandLine.Option(names = {"--report-path"}, paramLabel = "<path>")
    protected Path reportPath;

    @CommandLine.Option(names = {"--report-format"}, paramLabel = "<format>")
    String[] reportFormats;

    @Override
    protected C parent() {
        return parent;
    }

    protected int execute() {
        return 0;
    }

    protected Path resolveCacheDirectory() {
        cache = null != cache ? cache : Paths.get("cache");

        if (!cache.isAbsolute()) {
            Path basedir = Paths.get(".").normalize();
            cache = basedir.relativize(cache);
        }

        cache = cache.resolve("jarviz");

        try {
            Files.createDirectories(cache);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_CREATE_DIRECTORY", cache), e);
        }

        return cache;
    }

    protected Path resolveReportPath(Format format) {
        return Paths.get(reportPath.toAbsolutePath() + "." + format.toString().toLowerCase(Locale.ROOT));
    }

    protected List<Format> validateReportFormats() {
        if (null != reportPath && null == reportFormats || reportFormats.length == 0) {
            return Collections.singletonList(Format.TXT);
        }

        return collectEntries(reportFormats).stream()
            .map(Format::of)
            .collect(toList());
    }

    protected List<String> collectEntries(String[] input) {
        return collectEntries(input, false);
    }

    protected List<String> collectEntries(String[] input, boolean lowerCase) {
        List<String> list = new ArrayList<>();
        if (null != input && input.length > 0) {
            for (String s : input) {
                if (isNotBlank(s)) {
                    if (!s.contains("-") && lowerCase) {
                        s = getHyphenatedName(s);
                    }
                    list.add(lowerCase ? s.toLowerCase(Locale.ENGLISH) : s);
                }
            }
        }
        return list;
    }

    protected Formatter resolveFormatter(Format format) {
        switch (format) {
            case XML:
                return XmlFormatter.INSTANCE;
            case JSON:
                return JsonFormatter.INSTANCE;
            case YAML:
                return YamlFormatter.INSTANCE;
            case TXT:
            default:
                return TxtFormatter.INSTANCE;
        }
    }

    protected Node createRootNode(Path jarPath) {
        return Node.root($("report.key.jarviz"))
            .node($("report.key.subject"))
            .node($("report.key.file")).value(jarPath.getFileName()).end()
            .node($("report.key.size")).value(fileSize(jarPath)).end()
            .node($("report.key.sha256")).value(sha256(jarPath)).end()
            .end();
    }

    protected void writeReport(String content, Format format) {
        Path reportPath = resolveReportPath(format);
        try {
            Files.createDirectories(reportPath.getParent());
            Files.write(reportPath, content.getBytes(), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_WRITE_FILE", reportPath.toAbsolutePath()), e);
        }
    }

    protected String $$(String key, Object... args) {
        return colorize($(key, args));
    }

    protected String $b(boolean val) {
        return bool(val);
    }

    private long fileSize(Path jarPath) {
        try {
            return Files.size(jarPath);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_UNEXPECTED"), e);
        }
    }

    private String sha256(Path jarPath) {
          return ChecksumUtils.checksum(jarPath);
    }
}
