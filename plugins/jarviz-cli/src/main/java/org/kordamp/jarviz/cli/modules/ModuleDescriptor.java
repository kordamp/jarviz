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
import org.kordamp.jarviz.core.modules.DescriptorModuleJarProcessor;
import picocli.CommandLine;

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

    private static final String INDENT = "  ";
    private static final String SPACE = " ";
    private static final String EMPTY = "";

    @Override
    protected int execute() {
        DescriptorModuleJarProcessor processor = new DescriptorModuleJarProcessor(createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveOutputDirectory()));

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

        return 0;
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
