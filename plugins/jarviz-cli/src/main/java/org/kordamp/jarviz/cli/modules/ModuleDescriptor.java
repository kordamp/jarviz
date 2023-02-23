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

import org.kordamp.jarviz.cli.internal.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.processors.DescriptorModuleJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.kordamp.jarviz.cli.internal.Colorizer.magenta;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@CommandLine.Command(name = "descriptor")
public class ModuleDescriptor extends AbstractJarvizSubcommand<Module> {
    @Override
    protected int execute() {
        JarFileResolver jarFileResolver = createJarFileResolver();
        DescriptorModuleJarProcessor processor = new DescriptorModuleJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<java.lang.module.ModuleDescriptor>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(results);
        report(results);

        return 0;
    }

    private void output(Set<JarProcessor.JarFileResult<java.lang.module.ModuleDescriptor>> results) {
        Node root = createRootNode();
        for (JarProcessor.JarFileResult<java.lang.module.ModuleDescriptor> result : results) {
            if (null == outputFormat) {
                output(result);
            } else {
                buildReport(outputFormat, root, result);
            }
        }
        if (null != outputFormat) writeOutput(resolveFormatter(outputFormat).write(root));
    }

    private void output(JarProcessor.JarFileResult<java.lang.module.ModuleDescriptor> result) {
        parent().getOut().println($$("output.subject", result.getJarFileName()));
        java.lang.module.ModuleDescriptor md = result.getResult();

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

        parent().getOut().println($$("module.name", md.name()));
        md.version().ifPresent(v -> parent().getOut().println($$("module.version", v)));
        parent().getOut().println($$("module.open", $b(md.isOpen())));
        parent().getOut().println($$("module.automatic", $b(md.isAutomatic())));
        md.mainClass().ifPresent(c -> parent().getOut().println($$("module.main.class", c)));

        if (!unqualifiedExports.isEmpty()) {
            parent().getOut().println($$("module.exports"));
            unqualifiedExports.forEach(e -> parent().getOut()
                .println(INDENT + e.source() + magenta(toLowerCaseString(e.modifiers()))));
        }

        if (!md.requires().isEmpty()) {
            parent().getOut().println($$("module.requires"));
            md.requires().stream()
                .sorted().forEach(r -> parent().getOut()
                    .println(INDENT + r.name() + magenta(toLowerCaseString(r.modifiers()))));
        }

        if (!md.uses().isEmpty()) {
            parent().getOut().println($$("module.uses"));
            md.uses().stream()
                .sorted().forEach(s -> parent().getOut()
                    .println(INDENT + s));
        }

        if (!md.provides().isEmpty()) {
            parent().getOut().println($$("module.provides"));
            md.provides().stream()
                .sorted(comparing(java.lang.module.ModuleDescriptor.Provides::service))
                .forEach(p -> parent().getOut()
                    .println(INDENT + $$("module.provides.with", p.service(), toString(p.providers()))));
        }

        if (!qualifiedExports.isEmpty()) {
            parent().getOut().println($$("module.exports.qualified"));
            qualifiedExports
                .forEach(e -> parent().getOut()
                    .println(INDENT + $$("module.exports.to", e.source(), toLowerCaseString(e.targets()))));
        }

        if (!unqualifiedOpenPackages.isEmpty()) {
            parent().getOut().println($$("module.opens"));
            unqualifiedOpenPackages.forEach(p -> parent().getOut()
                .println(INDENT + p.source() + magenta(toLowerCaseString(p.modifiers()))));
        }

        if (!qualifiedOpenPackages.isEmpty()) {
            parent().getOut().println($$("module.opens.qualified"));
            qualifiedOpenPackages
                .forEach(e -> parent().getOut()
                    .println(INDENT + $$("module.opens.to", e.source(), toLowerCaseString(e.targets()))));
        }

        if (!hiddenPackages.isEmpty()) {
            parent().getOut().println($$("module.contains"));
            hiddenPackages.forEach(p -> parent().getOut()
                .println(INDENT + p));
        }
    }

    private void report(Set<JarProcessor.JarFileResult<java.lang.module.ModuleDescriptor>> results) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<java.lang.module.ModuleDescriptor> result : results) {
                buildReport(format, root, result);
            }
            writeReport(resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Format format, Node root, JarProcessor.JarFileResult<java.lang.module.ModuleDescriptor> result) {
        java.lang.module.ModuleDescriptor md = result.getResult();

        appendSubject(root, result.getJarPath(), "module descriptor", resultNode -> {
            Node module = resultNode.node($("report.key.module"));
            module.node($("report.key.name")).value(md.name()).end();
            md.version().ifPresent(v -> module.node($("report.key.version")).value(v).end());
            module.node($("report.key.open")).value(md.isOpen()).end();
            module.node($("report.key.automatic")).value(md.isAutomatic()).end();
            md.mainClass().ifPresent(c -> module.node($("report.key.main.class")).value(c).end());

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
                Node exports = module.array($("report.key.exports"));
                unqualifiedExports.forEach(e -> {
                    if (format == Format.TXT) {
                        exports.node(e.source() + toLowerCaseString(e.modifiers())).end();
                    } else {
                        exports.collapsable($("report.key.export"))
                            .node($("report.key.package")).value(e.source()).end()
                            .node($("report.key.modifiers")).value(toLowerCaseString(e.modifiers())).end()
                            .cleanup();
                    }
                });
                exports.end();
            }

            if (!md.requires().isEmpty()) {
                Node requires = module.array($("report.key.requires"));
                md.requires().forEach(r -> {
                    if (format == Format.TXT) {
                        requires.node(r.name() + toLowerCaseString(r.modifiers())).end();
                    } else {
                        requires.collapsable($("report.key.require"))
                            .node($("report.key.module")).value(r.name()).end()
                            .node($("report.key.modifiers")).value(toLowerCaseString(r.modifiers())).end()
                            .cleanup();
                    }
                });
                requires.end();
            }

            if (!md.uses().isEmpty() || !md.provides().isEmpty()) {
                Node services = module.node($("report.key.services"));

                if (!md.uses().isEmpty()) {
                    Node requires = services.array($("report.key.uses"));
                    md.uses().forEach(s -> {
                        if (format == Format.TXT) {
                            requires.node(s).end();
                        } else {
                            requires.collapsable($("report.key.service")).value(s).end();
                        }
                    });
                    requires.end();
                }

                if (!md.provides().isEmpty()) {
                    Node provides = services.array($("report.key.provides"));
                    md.provides().stream()
                        .sorted(comparing(java.lang.module.ModuleDescriptor.Provides::service))
                        .forEach(p -> {
                            if (format == Format.TXT) {
                                provides.node(p.service()).children(p.providers()).end();
                            } else {
                                provides.collapsable($("report.key.provider"))
                                    .node($("report.key.service")).value(p.service()).end()
                                    .array($("report.key.implementations")).collapsableChildren($("report.key.implementation"), p.providers()).end()
                                    .cleanup();
                            }
                        });
                    provides.end();
                }
            }

            if (!unqualifiedOpenPackages.isEmpty()) {
                Node opens = module.array($("report.key.opens"));
                unqualifiedOpenPackages.forEach(e -> {
                    if (format == Format.TXT) {
                        opens.node(e.source() + toLowerCaseString(e.modifiers())).end();
                    } else {
                        opens.collapsable($("report.key.open"))
                            .node($("report.key.module")).value(e.source()).end()
                            .node($("report.key.modifiers")).value(toLowerCaseString(e.modifiers())).end()
                            .cleanup();
                    }
                });
                opens.end();
            }

            if (!qualifiedExports.isEmpty() || !qualifiedOpenPackages.isEmpty()) {
                Node qualified = module.node($("report.key.qualified"));

                if (!qualifiedExports.isEmpty()) {
                    Node exports = qualified.array($("report.key.exports"));
                    qualifiedExports
                        .forEach(e -> {
                            if (format == Format.TXT) {
                                exports.node(e.source()).children(e.targets()).end();
                            } else {
                                exports.collapsable($("report.key.export"))
                                    .node($("report.key.package")).value(e.source()).end()
                                    .array($("report.key.targets")).collapsableChildren($("report.key.target"), e.targets()).end()
                                    .end();
                            }
                        });
                    exports.end();
                }

                if (!qualifiedOpenPackages.isEmpty()) {
                    Node exports = qualified.array($("report.key.opens"));
                    qualifiedOpenPackages
                        .forEach(e -> {
                            if (format == Format.TXT) {
                                exports.node(e.source()).children(e.targets()).end();
                            } else {
                                exports.collapsable($("report.key.open"))
                                    .node($("report.key.package")).value(e.source()).end()
                                    .array($("report.key.targets")).collapsableChildren($("report.key.target"), e.targets()).end()
                                    .end();
                            }
                        });
                    exports.end();
                }
            }

            if (!hiddenPackages.isEmpty()) {
                Node contains = module.array($("report.key.contains"));
                hiddenPackages.forEach(s -> {
                    if (format == Format.TXT) {
                        contains.node(s).end();
                    } else {
                        contains.collapsable($("report.key.package")).value(s).end();
                    }
                });
                contains.end();
            }
        });
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
