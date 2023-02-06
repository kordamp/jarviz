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
package org.kordamp.jarviz.cli.modules;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.cli.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.modules.DescriptorModuleJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.kordamp.jarviz.core.resolvers.JarFileResolvers.createJarFileResolver;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CommandLine.Command(name = "descriptor")
public class ModuleDescriptor extends AbstractJarvizSubcommand<Module> {
    @Override
    protected int execute() {
        JarFileResolver<?> jarFileResolver = createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveCacheDirectory());
        DescriptorModuleJarProcessor processor = new DescriptorModuleJarProcessor(jarFileResolver);

        java.lang.module.ModuleDescriptor md = processor.getResult();

        List<java.lang.module.ModuleDescriptor.Exports> unqualifiedExports = md.exports().stream()
            .sorted(comparing(java.lang.module.ModuleDescriptor.Exports::source))
            .filter(e -> !e.isQualified())
            .collect(toList());

        List<java.lang.module.ModuleDescriptor.Exports> qualifiedExports = md.exports().stream()
            .sorted(comparing(java.lang.module.ModuleDescriptor.Exports::source))
            .filter(java.lang.module.ModuleDescriptor.Exports::isQualified)
            .collect(toList());

        List<java.lang.module.ModuleDescriptor.Opens> unqualifiedOpenPackages = md.opens().stream()
            .sorted(comparing(java.lang.module.ModuleDescriptor.Opens::source))
            .filter(o -> !o.isQualified())
            .collect(toList());

        List<java.lang.module.ModuleDescriptor.Opens> qualifiedOpenPackages = md.opens().stream()
            .sorted(comparing(java.lang.module.ModuleDescriptor.Opens::source))
            .filter(java.lang.module.ModuleDescriptor.Opens::isQualified)
            .collect(toList());

        Set<String> hiddenPackages = new TreeSet<>(md.packages());
        md.exports().stream().map(java.lang.module.ModuleDescriptor.Exports::source).forEach(hiddenPackages::remove);
        md.opens().stream().map(java.lang.module.ModuleDescriptor.Opens::source).forEach(hiddenPackages::remove);

        parent().getOut().println(RB.$("module.name", md.name()));
        md.version().ifPresent(v -> parent().getOut().println(RB.$("module.version", v)));
        parent().getOut().println(RB.$("module.open", md.isOpen()));
        parent().getOut().println(RB.$("module.automatic", md.isAutomatic()));
        md.mainClass().ifPresent(c -> parent().getOut().println(RB.$("module.main.class", c)));

        if (!unqualifiedExports.isEmpty()) {
            parent().getOut().println(RB.$("module.exports"));
            unqualifiedExports.forEach(e -> parent().getOut().println(INDENT + e.source() + toLowerCaseString(e.modifiers())));
        }

        if (!md.requires().isEmpty()) {
            parent().getOut().println(RB.$("module.requires"));
            md.requires().stream()
                .sorted().forEach(r -> parent().getOut().println(INDENT + r.name() + toLowerCaseString(r.modifiers())));
        }

        if (!md.uses().isEmpty()) {
            parent().getOut().println(RB.$("module.uses"));
            md.uses().stream()
                .sorted().forEach(s -> parent().getOut().println(INDENT + s));
        }

        if (!md.provides().isEmpty()) {
            parent().getOut().println(RB.$("module.provides"));
            md.provides().stream()
                .sorted(comparing(java.lang.module.ModuleDescriptor.Provides::service))
                .forEach(p -> parent().getOut().println(INDENT + RB.$("module.provides.with", p.service(), toString(p.providers()))));
        }

        if (!qualifiedExports.isEmpty()) {
            parent().getOut().println(RB.$("module.exports.qualified"));
            qualifiedExports
                .forEach(e -> parent().getOut().println(INDENT + RB.$("module.exports.to", e.source(), toLowerCaseString(e.targets()))));
        }

        if (!unqualifiedOpenPackages.isEmpty()) {
            parent().getOut().println(RB.$("module.opens"));
            unqualifiedOpenPackages.forEach(p -> parent().getOut().println(INDENT + p.source() + toLowerCaseString(p.modifiers())));
        }

        if (!qualifiedOpenPackages.isEmpty()) {
            parent().getOut().println(RB.$("module.opens.qualified"));
            qualifiedOpenPackages
                .forEach(e -> parent().getOut().println(INDENT + RB.$("module.opens.to", e.source(), toLowerCaseString(e.targets()))));
        }

        if (!hiddenPackages.isEmpty()) {
            parent().getOut().println(RB.$("module.contains"));
            hiddenPackages.forEach(p -> parent().getOut().println(INDENT + p));
        }

        report(jarFileResolver, md);

        return 0;
    }

    private void report(JarFileResolver<?> jarFileResolver, java.lang.module.ModuleDescriptor md) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node node = buildReport(format, Paths.get(jarFileResolver.resolveJarFile().getName()), md);
            writeReport(resolveFormatter(format).write(node), format);
        }
    }

    private Node buildReport(Format format, Path jarPath, java.lang.module.ModuleDescriptor md) {
        Node root = createRootNode(jarPath);
        Node module = root.node(RB.$("report.key.module"));
        module.node(RB.$("report.key.name")).value(md.name()).end();
        md.version().ifPresent(v -> module.node(RB.$("report.key.version")).value(v).end());
        module.node(RB.$("report.key.open")).value(md.isOpen()).end();
        module.node(RB.$("report.key.automatic")).value(md.isAutomatic()).end();
        md.mainClass().ifPresent(c -> module.node(RB.$("report.key.main.class")).value(c).end());

        List<java.lang.module.ModuleDescriptor.Exports> unqualifiedExports = md.exports().stream()
            .sorted(comparing(java.lang.module.ModuleDescriptor.Exports::source))
            .filter(e -> !e.isQualified())
            .collect(toList());

        List<java.lang.module.ModuleDescriptor.Exports> qualifiedExports = md.exports().stream()
            .sorted(comparing(java.lang.module.ModuleDescriptor.Exports::source))
            .filter(java.lang.module.ModuleDescriptor.Exports::isQualified)
            .collect(toList());

        List<java.lang.module.ModuleDescriptor.Opens> unqualifiedOpenPackages = md.opens().stream()
            .sorted(comparing(java.lang.module.ModuleDescriptor.Opens::source))
            .filter(o -> !o.isQualified())
            .collect(toList());

        List<java.lang.module.ModuleDescriptor.Opens> qualifiedOpenPackages = md.opens().stream()
            .sorted(comparing(java.lang.module.ModuleDescriptor.Opens::source))
            .filter(java.lang.module.ModuleDescriptor.Opens::isQualified)
            .collect(toList());

        Set<String> hiddenPackages = new TreeSet<>(md.packages());
        md.exports().stream().map(java.lang.module.ModuleDescriptor.Exports::source).forEach(hiddenPackages::remove);
        md.opens().stream().map(java.lang.module.ModuleDescriptor.Opens::source).forEach(hiddenPackages::remove);

        if (!unqualifiedExports.isEmpty()) {
            Node exports = module.array(RB.$("report.key.exports"));
            unqualifiedExports.forEach(e -> {
                if (format == Format.TXT) {
                    exports.node(e.source() + toLowerCaseString(e.modifiers())).end();
                } else {
                    exports.collapsable(RB.$("report.key.export"))
                        .node(RB.$("report.key.package")).value(e.source()).end()
                        .node(RB.$("report.key.modifiers")).value(toLowerCaseString(e.modifiers())).end()
                        .cleanup();
                }
            });
            exports.end();
        }

        if (!md.requires().isEmpty()) {
            Node requires = module.array(RB.$("report.key.requires"));
            md.requires().forEach(r -> {
                if (format == Format.TXT) {
                    requires.node(r.name() + toLowerCaseString(r.modifiers())).end();
                } else {
                    requires.collapsable(RB.$("report.key.require"))
                        .node(RB.$("report.key.module")).value(r.name()).end()
                        .node(RB.$("report.key.modifiers")).value(toLowerCaseString(r.modifiers())).end()
                        .cleanup();
                }
            });
            requires.end();
        }

        if (!md.uses().isEmpty() || !md.provides().isEmpty()) {
            Node services = module.node(RB.$("report.key.services"));

            if (!md.uses().isEmpty()) {
                Node requires = services.array(RB.$("report.key.uses"));
                md.uses().forEach(s -> {
                    if (format == Format.TXT) {
                        requires.node(s).end();
                    } else {
                        requires.collapsable(RB.$("report.key.service")).value(s).end();
                    }
                });
                requires.end();
            }

            if (!md.provides().isEmpty()) {
                Node provides = services.array(RB.$("report.key.provides"));
                md.provides().stream()
                    .sorted(comparing(java.lang.module.ModuleDescriptor.Provides::service))
                    .forEach(p -> {
                        if (format == Format.TXT) {
                            provides.node(p.service()).children(p.providers()).end();
                        } else {
                            provides.collapsable(RB.$("report.key.provider"))
                                .node(RB.$("report.key.service")).value(p.service()).end()
                                .array(RB.$("report.key.implementations")).collapsableChildren(RB.$("report.key.implementation"), p.providers()).end()
                                .cleanup();
                        }
                    });
                provides.end();
            }
        }

        if (!unqualifiedOpenPackages.isEmpty()) {
            Node opens = module.array(RB.$("report.key.opens"));
            unqualifiedOpenPackages.forEach(e -> {
                if (format == Format.TXT) {
                    opens.node(e.source() + toLowerCaseString(e.modifiers())).end();
                } else {
                    opens.collapsable(RB.$("report.key.open"))
                        .node(RB.$("report.key.module")).value(e.source()).end()
                        .node(RB.$("report.key.modifiers")).value(toLowerCaseString(e.modifiers())).end()
                        .cleanup();
                }
            });
            opens.end();
        }

        if (!qualifiedExports.isEmpty() || !qualifiedOpenPackages.isEmpty()) {
            Node qualified = module.node(RB.$("report.key.qualified"));

            if (!qualifiedExports.isEmpty()) {
                Node exports = qualified.array(RB.$("report.key.exports"));
                qualifiedExports
                    .forEach(e -> {
                        if (format == Format.TXT) {
                            exports.node(e.source()).children(e.targets()).end();
                        } else {
                            exports.collapsable(RB.$("report.key.export"))
                                .node(RB.$("report.key.package")).value(e.source()).end()
                                .array(RB.$("report.key.targets")).collapsableChildren(RB.$("report.key.targets"), e.targets()).end()
                                .end();
                        }
                    });
                exports.end();
            }

            if (!qualifiedOpenPackages.isEmpty()) {
                Node exports = qualified.array(RB.$("report.key.opens"));
                qualifiedOpenPackages
                    .forEach(e -> {
                        if (format == Format.TXT) {
                            exports.node(e.source()).children(e.targets()).end();
                        } else {
                            exports.collapsable(RB.$("report.key.open"))
                                .node(RB.$("report.key.package")).value(e.source()).end()
                                .array(RB.$("report.key.targets")).collapsableChildren(RB.$("report.key.targets"), e.targets()).end()
                                .end();
                        }
                    });
                exports.end();
            }
        }

        if (!hiddenPackages.isEmpty()) {
            Node contains = module.array(RB.$("report.key.contains"));
            hiddenPackages.forEach(s -> {
                if (format == Format.TXT) {
                    contains.node(s).end();
                } else {
                    contains.collapsable(RB.$("report.key.package")).value(s).end();
                }
            });
            contains.end();
        }

        return root;
    }

    private <T> String toLowerCaseString(Collection<T> set) {
        if (set.isEmpty()) {
            return EMPTY;
        }
        return SPACE + set.stream()
            .map(e -> e.toString().toLowerCase(Locale.ROOT))
            .sorted()
            .collect(joining(SPACE));
    }

    private <T> String toString(Collection<T> set) {
        if (set.isEmpty()) {
            return EMPTY;
        }
        return SPACE + set.stream()
            .map(Object::toString)
            .sorted()
            .collect(joining(SPACE));
    }
}
