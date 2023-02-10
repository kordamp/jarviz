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
package org.kordamp.jarviz.cli.bytecode;

import org.kordamp.jarviz.cli.internal.AbstractJarvizSubcommand;
import org.kordamp.jarviz.cli.internal.Colorizer;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.bytecode.ShowBytecodeJarProcessor;
import org.kordamp.jarviz.core.model.BytecodeVersions;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static org.kordamp.jarviz.core.JarFileResolvers.createJarFileResolver;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "show")
public class BytecodeShow extends AbstractJarvizSubcommand<Bytecode> {
    @CommandLine.Option(names = {"--details"})
    public boolean details;

    @CommandLine.Option(names = {"--bytecode-version"}, paramLabel = "<version>")
    public Integer bytecodeVersion;

    @CommandLine.Option(names = {"--java-version"}, paramLabel = "<version>")
    public Integer javaVersion;

    @Override
    protected int execute() {
        JarFileResolver<?> jarFileResolver = createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveCacheDirectory());
        ShowBytecodeJarProcessor processor = new ShowBytecodeJarProcessor(jarFileResolver);

        BytecodeVersions bytecodeVersions = processor.getResult();

        Integer bc = bytecodeVersion != null && bytecodeVersion > 43 ? bytecodeVersion : 0;
        Integer jv = javaVersion != null && javaVersion > 8 ? javaVersion : 0;

        if (bc == 0 && jv == 0) {
            Set<Integer> manifestBytecode = bytecodeVersions.getManifestBytecode();
            if (manifestBytecode.size() > 0) {
                parent().getOut().println($$("bytecode.version.attribute", manifestBytecode.stream()
                    .map(String::valueOf)
                    .map(Colorizer::cyan)
                    .collect(joining(","))));
            }
        }

        if (jv == 0) {
            Map<Integer, List<String>> unversionedClasses = bytecodeVersions.getUnversionedClasses();
            if (bc == 0) {
                unversionedClasses.keySet().stream()
                    .sorted()
                    .forEach(bytecodeVersion -> printUnversioned(unversionedClasses, bytecodeVersion));
            } else {
                printUnversioned(unversionedClasses, bc);
            }
        }

        Set<Integer> javaVersions = bytecodeVersions.getJavaVersionOfVersionedClasses();
        if (jv == 0) {
            for (Integer javaVersion : javaVersions) {
                Map<Integer, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(javaVersion);
                if (bc == 0) {
                    for (Map.Entry<Integer, List<String>> entry : versionedClasses.entrySet()) {
                        printVersioned(versionedClasses, javaVersion, entry.getKey());
                    }
                } else {
                    printVersioned(versionedClasses, javaVersion, bc);
                }
            }
        } else {
            Map<Integer, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(jv);
            if (bc == 0) {
                for (Map.Entry<Integer, List<String>> entry : versionedClasses.entrySet()) {
                    printVersioned(versionedClasses, jv, entry.getKey());
                }
            } else {
                printVersioned(versionedClasses, jv, bc);
            }
        }

        report(jarFileResolver, bytecodeVersions);

        return 0;
    }

    private void printUnversioned(Map<Integer, List<String>> unversionedClasses, Integer bytecodeVersion) {
        if (!unversionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = unversionedClasses.get(bytecodeVersion);
        parent().getOut().println($$("bytecode.unversioned.classes.total", bytecodeVersion, classes.size()));
        if (details) {
            classes.forEach(parent().getOut()::println);
        }
    }

    private void printVersioned(Map<Integer, List<String>> versionedClasses, Integer javaVersion, Integer bytecodeVersion) {
        if (!versionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = versionedClasses.get(bytecodeVersion);
        parent().getOut().println($$("bytecode.versioned.classes.total", javaVersion, bytecodeVersion, classes.size()));
        if (details) {
            classes.forEach(parent().getOut()::println);
        }
    }

    private void report(JarFileResolver<?> jarFileResolver, BytecodeVersions bytecodeVersions) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node node = buildReport(format, Paths.get(jarFileResolver.resolveJarFile().getName()), bytecodeVersions);
            writeReport(resolveFormatter(format).write(node), format);
        }
    }

    private Node buildReport(Format format, Path jarPath, BytecodeVersions bytecodeVersions) {
        Node root = createRootNode(jarPath);

        Integer bc = bytecodeVersion != null && bytecodeVersion > 43 ? bytecodeVersion : 0;
        Integer jv = javaVersion != null && javaVersion > 8 ? javaVersion : 0;

        if (bc == 0 && jv == 0) {
            Set<Integer> manifestBytecode = bytecodeVersions.getManifestBytecode();
            if (manifestBytecode.size() > 0) {
                Node bytecode = root.array($("report.key.bytecode"));
                manifestBytecode.stream()
                    .map(String::valueOf)
                    .forEach(v -> {
                        if (format == Format.TXT) {
                            bytecode.node(v).end();
                        } else {
                            bytecode.collapsable($("report.key.version")).value(v).end();
                        }
                    });
            }
        }

        if (jv == 0) {
            Map<Integer, List<String>> unversionedClasses = bytecodeVersions.getUnversionedClasses();
            if (bc == 0) {
                unversionedClasses.keySet().stream()
                    .sorted()
                    .forEach(bytecodeVersion -> reportUnversioned(root, unversionedClasses, bytecodeVersion));
            } else {
                reportUnversioned(root, unversionedClasses, bc);
            }
        }

        Set<Integer> javaVersions = bytecodeVersions.getJavaVersionOfVersionedClasses();
        if (jv == 0) {
            for (Integer javaVersion : javaVersions) {
                Map<Integer, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(javaVersion);
                if (bc == 0) {
                    for (Map.Entry<Integer, List<String>> entry : versionedClasses.entrySet()) {
                        reportVersioned(root, versionedClasses, javaVersion, entry.getKey());
                    }
                } else {
                    reportVersioned(root, versionedClasses, javaVersion, bc);
                }
            }
        } else {
            Map<Integer, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(jv);
            if (bc == 0) {
                for (Map.Entry<Integer, List<String>> entry : versionedClasses.entrySet()) {
                    reportVersioned(root, versionedClasses, jv, entry.getKey());
                }
            } else {
                reportVersioned(root, versionedClasses, jv, bc);
            }
        }

        return root;
    }

    private void reportUnversioned(Node root, Map<Integer, List<String>> unversionedClasses, Integer bytecodeVersion) {
        if (!unversionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = unversionedClasses.get(bytecodeVersion);
        Node unversioned = root.node($("report.key.unversioned"))
            .node($("report.key.bytecode")).value(bytecodeVersion).end()
            .node($("report.key.total")).value(classes.size()).end();

        if (details) {
            unversioned.array($("report.key.classes"))
                .collapsableChildren($("report.key.class"), classes);
        }
    }

    private void reportVersioned(Node root, Map<Integer, List<String>> versionedClasses, Integer javaVersion, Integer bytecodeVersion) {
        if (!versionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = versionedClasses.get(bytecodeVersion);
        Node versioned = root.node($("report.key.versioned"))
            .node($("report.key.bytecode")).value(bytecodeVersion).end()
            .node($("report.key.total")).value(classes.size()).end();

        if (details) {
            versioned.array($("report.key.classes"))
                .collapsableChildren($("report.key.class"), classes);
        }
    }
}
