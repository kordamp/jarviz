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
package org.kordamp.jarviz.core.internal;

import org.kordamp.jarviz.core.InsufficientInputsException;
import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.Format;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.core.resolvers.JarFileResolvers;
import org.kordamp.jarviz.reporting.Formatter;
import org.kordamp.jarviz.reporting.JsonFormatter;
import org.kordamp.jarviz.reporting.Node;
import org.kordamp.jarviz.reporting.TxtFormatter;
import org.kordamp.jarviz.reporting.XmlFormatter;
import org.kordamp.jarviz.reporting.YamlFormatter;
import org.kordamp.jarviz.util.Algorithm;
import org.kordamp.jarviz.util.ChecksumUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.kordamp.jarviz.core.internal.Colorizer.bool;
import static org.kordamp.jarviz.core.internal.Colorizer.colorize;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class AbstractCommand<C extends Configuration<C>> {
    public static final String INDENT = "  ";
    public static final String SPACE = " ";
    public static final String EMPTY = "";

    public abstract int execute(C configuration);

    protected Path resolveCacheDirectory(C configuration) {
        Path cache = configuration.getCacheDirectory();
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

    protected Path resolveReportPath(C configuration, Format format) {
        Path reportPath = configuration.getReportPath();
        return Paths.get(reportPath.toAbsolutePath() + "." + format.toString().toLowerCase(Locale.ROOT));
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

    protected Node createRootNode() {
        return Node.root(RB.$("report.key.jarviz"));
    }

    protected Node appendSubject(Node root, Path jarPath, String command, Consumer<Node> result) {
        Node subjects = root.getChildren().isEmpty() ? root.array(RB.$("report.key.subjects")) : root.getChildren().get(0);
        Node resultNode = subjects
            .collapsable(RB.$("report.key.subject"))
            .node(RB.$("report.key.command")).value(command).end()
            .node(RB.$("report.key.jar"))
                .node(RB.$("report.key.file")).value(jarPath.getFileName()).end()
                .node(RB.$("report.key.size")).value(fileSize(jarPath)).end()
                .node(RB.$("report.key.sha256")).value(sha256(jarPath)).end()
            .end()
            .node(RB.$("report.key.result"));
        result.accept(resultNode);

        return root;
    }

    protected void writeOutput(C configuration, String content) {
        configuration.getOut().println(content);
    }

    protected void writeReport(C configuration, String content, Format format) {
        Path reportPath = resolveReportPath(configuration, format);
        try {
            Files.createDirectories(reportPath.getParent());
            Files.write(reportPath, content.getBytes(), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_WRITE_FILE", reportPath.toAbsolutePath()), e);
        }
    }

    protected String $$(String key, Object... args) {
        return colorize(RB.$(key, args));
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
        return ChecksumUtils.checksum(Algorithm.SHA_256, jarPath);
    }

    protected JarFileResolver createJarFileResolver(C configuration) {
        Set<JarFileResolver> resolvers = new LinkedHashSet<>();
        Path cacheDirectory = resolveCacheDirectory(configuration);
        resolvers.addAll(JarFileResolvers.gavJarFileResolvers(cacheDirectory, configuration.getGavs()));
        resolvers.addAll(JarFileResolvers.pathJarFileResolvers(configuration.getFiles()));
        resolvers.addAll(JarFileResolvers.directoryJarFileResolvers(configuration.getDirectories()));
        resolvers.addAll(JarFileResolvers.classpathJarFileResolvers(configuration.getClasspaths()));
        resolvers.addAll(JarFileResolvers.urlJarFileResolvers(cacheDirectory, configuration.getUrls()));

        if (resolvers.isEmpty()) {
            throw new InsufficientInputsException($$("ERROR_INSUFFICIENT_INPUTS"));
        }

        return JarFileResolvers.compositeJarFileResolver(resolvers);
    }
}
