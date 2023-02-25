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
package org.kordamp.jarviz.commands;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.Format;
import org.kordamp.jarviz.core.internal.AbstractCommand;
import org.kordamp.jarviz.core.internal.AbstractConfiguration;
import org.kordamp.jarviz.core.model.ModuleMetadata;
import org.kordamp.jarviz.core.processors.JarProcessor;
import org.kordamp.jarviz.core.processors.ModuleDescriptorJarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.kordamp.jarviz.core.internal.Colorizer.magenta;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ModuleDescriptorCommand extends AbstractCommand<ModuleDescriptorCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {

    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        ModuleDescriptorJarProcessor processor = new ModuleDescriptorJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<ModuleMetadata>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(configuration, results);
        report(configuration, results);

        return 0;
    }

    private void output(Configuration configuration, Set<JarProcessor.JarFileResult<ModuleMetadata>> results) {
        Node root = createRootNode();
        Format outputFormat = configuration.getOutputFormat();
        for (JarProcessor.JarFileResult<ModuleMetadata> result : results) {
            if (null == outputFormat) {
                output(configuration, result);
            } else {
                buildReport(outputFormat, root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<ModuleMetadata> result) {
        configuration.getOut().println($$("output.subject", result.getJarFileName()));

        org.kordamp.jarviz.core.model.ModuleName moduleName = result.getResult().getModuleName();
        if (!moduleName.isValid()) {
            configuration.getOut().println($$("module.name", moduleName.getModuleName()));
            configuration.getOut().println($$("module.source", moduleName.resolveSource()));
            configuration.getOut().println($$("module.automatic", $b(moduleName.isAutomatic())));
            configuration.getOut().println($$("module.valid", $b(moduleName.isValid())));
            configuration.getOut().println($$("module.reason", moduleName.getReason()));
            return;
        }

        java.lang.module.ModuleDescriptor md = result.getResult().getModuleDescriptor().get();

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

        configuration.getOut().println($$("module.name", md.name()));
        md.version().ifPresent(v -> configuration.getOut().println($$("module.version", v)));
        configuration.getOut().println($$("module.open", $b(md.isOpen())));
        configuration.getOut().println($$("module.automatic", $b(md.isAutomatic())));
        md.mainClass().ifPresent(c -> configuration.getOut().println($$("module.main.class", c)));

        if (!unqualifiedExports.isEmpty()) {
            configuration.getOut().println($$("module.exports"));
            unqualifiedExports.forEach(e -> configuration.getOut()
                .println(INDENT + e.source() + magenta(toLowerCaseString(e.modifiers()))));
        }

        if (!md.requires().isEmpty()) {
            configuration.getOut().println($$("module.requires"));
            md.requires().stream()
                .sorted().forEach(r -> configuration.getOut()
                    .println(INDENT + r.name() + magenta(toLowerCaseString(r.modifiers()))));
        }

        if (!md.uses().isEmpty()) {
            configuration.getOut().println($$("module.uses"));
            md.uses().stream()
                .sorted().forEach(s -> configuration.getOut()
                    .println(INDENT + s));
        }

        if (!md.provides().isEmpty()) {
            configuration.getOut().println($$("module.provides"));
            md.provides().stream()
                .sorted(comparing(java.lang.module.ModuleDescriptor.Provides::service))
                .forEach(p -> configuration.getOut()
                    .println(INDENT + $$("module.provides.with", p.service(), toString(p.providers()))));
        }

        if (!qualifiedExports.isEmpty()) {
            configuration.getOut().println($$("module.exports.qualified"));
            qualifiedExports
                .forEach(e -> configuration.getOut()
                    .println(INDENT + $$("module.exports.to", e.source(), toLowerCaseString(e.targets()))));
        }

        if (!unqualifiedOpenPackages.isEmpty()) {
            configuration.getOut().println($$("module.opens"));
            unqualifiedOpenPackages.forEach(p -> configuration.getOut()
                .println(INDENT + p.source() + magenta(toLowerCaseString(p.modifiers()))));
        }

        if (!qualifiedOpenPackages.isEmpty()) {
            configuration.getOut().println($$("module.opens.qualified"));
            qualifiedOpenPackages
                .forEach(e -> configuration.getOut()
                    .println(INDENT + $$("module.opens.to", e.source(), toLowerCaseString(e.targets()))));
        }

        if (!hiddenPackages.isEmpty()) {
            configuration.getOut().println($$("module.contains"));
            hiddenPackages.forEach(p -> configuration.getOut()
                .println(INDENT + p));
        }
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<ModuleMetadata>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<ModuleMetadata> result : results) {
                buildReport(format, root, result);
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Format format, Node root, JarProcessor.JarFileResult<ModuleMetadata> result) {
        appendSubject(root, result.getJarPath(), "module descriptor", resultNode -> {
            org.kordamp.jarviz.core.model.ModuleName moduleName = result.getResult().getModuleName();
            if (!moduleName.isValid()) {
                resultNode.node(RB.$("report.key.name")).value(moduleName.getModuleName()).end()
                    .node(RB.$("report.key.source")).value(moduleName.resolveSource()).end()
                    .node(RB.$("report.key.automatic")).value(moduleName.isAutomatic()).end()
                    .node(RB.$("report.key.valid")).value(moduleName.isValid()).end()
                    .node(RB.$("report.key.reason")).value(moduleName.getReason()).end();
                return;
            }

            java.lang.module.ModuleDescriptor md = result.getResult().getModuleDescriptor().get();

            Node module = resultNode.node(RB.$("report.key.module"));
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
                                    .array(RB.$("report.key.targets")).collapsableChildren(RB.$("report.key.target"), e.targets()).end()
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
                                    .array(RB.$("report.key.targets")).collapsableChildren(RB.$("report.key.target"), e.targets()).end()
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
