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
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.model.BytecodeVersion;
import org.kordamp.jarviz.core.model.BytecodeVersions;
import org.kordamp.jarviz.core.processors.ShowBytecodeJarProcessor;
import org.kordamp.jarviz.reporting.Format;
import org.kordamp.jarviz.reporting.Node;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

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
        JarFileResolver jarFileResolver = createJarFileResolver();
        ShowBytecodeJarProcessor processor = new ShowBytecodeJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<BytecodeVersions>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(results);
        report(results);

        return 0;
    }

    private void output(Set<JarProcessor.JarFileResult<BytecodeVersions>> results) {
        Node root = createRootNode();
        for (JarProcessor.JarFileResult<BytecodeVersions> result : results) {
            if (null == outputFormat) {
                output(result);
            } else {
                buildReport(outputFormat, root, result);
            }
        }
        if (null != outputFormat) writeOutput(resolveFormatter(outputFormat).write(root));
    }

    private void output(JarProcessor.JarFileResult<BytecodeVersions> result) {
        parent().getOut().println($$("output.subject", result.getJarFileName()));
        BytecodeVersions bytecodeVersions = result.getResult();

        BytecodeVersion bc = BytecodeVersion.of(bytecodeVersion != null && bytecodeVersion > 43 ? bytecodeVersion : 0);
        Integer jv = javaVersion != null && javaVersion > 8 ? javaVersion : 0;

        if (bc.isEmpty() && 0 == jv) {
            Set<BytecodeVersion> manifestBytecode = bytecodeVersions.getManifestBytecode();
            if (manifestBytecode.size() > 0) {
                parent().getOut().println($$("bytecode.version.attribute", manifestBytecode.stream()
                    .map(String::valueOf)
                    .map(Colorizer::cyan)
                    .collect(joining(","))));
            }
        }

        if (0 == jv) {
            Map<BytecodeVersion, List<String>> unversionedClasses = bytecodeVersions.getUnversionedClasses();
            if (bc.isEmpty()) {
                unversionedClasses.keySet().stream()
                    .sorted()
                    .forEach(bytecodeVersion -> printUnversioned(unversionedClasses, bytecodeVersion));
            } else {
                printUnversioned(unversionedClasses, bc);
            }
        }

        Set<Integer> javaVersions = bytecodeVersions.getJavaVersionOfVersionedClasses();
        if (0 == jv) {
            for (Integer javaVersion : javaVersions) {
                Map<BytecodeVersion, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(javaVersion);
                if (bc.isEmpty()) {
                    for (Map.Entry<BytecodeVersion, List<String>> entry : versionedClasses.entrySet()) {
                        printVersioned(versionedClasses, javaVersion, entry.getKey());
                    }
                } else {
                    printVersioned(versionedClasses, javaVersion, bc);
                }
            }
        } else {
            Map<BytecodeVersion, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(jv);
            if (bc.isEmpty()) {
                for (Map.Entry<BytecodeVersion, List<String>> entry : versionedClasses.entrySet()) {
                    printVersioned(versionedClasses, jv, entry.getKey());
                }
            } else {
                printVersioned(versionedClasses, jv, bc);
            }
        }
    }

    private void printUnversioned(Map<BytecodeVersion, List<String>> unversionedClasses, BytecodeVersion bytecodeVersion) {
        if (!unversionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = unversionedClasses.get(bytecodeVersion);
        parent().getOut().println($$("bytecode.unversioned.classes.total", bytecodeVersion, classes.size()));
        if (details) {
            classes.forEach(parent().getOut()::println);
        }
    }

    private void printVersioned(Map<BytecodeVersion, List<String>> versionedClasses, Integer javaVersion, BytecodeVersion bytecodeVersion) {
        if (!versionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = versionedClasses.get(bytecodeVersion);
        parent().getOut().println($$("bytecode.versioned.classes.total", javaVersion, bytecodeVersion, classes.size()));
        if (details) {
            classes.forEach(parent().getOut()::println);
        }
    }

    private void report(Set<JarProcessor.JarFileResult<BytecodeVersions>> results) {
        if (null == reportPath) return;

        for (Format format : validateReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<BytecodeVersions> result : results) {
                buildReport(format, root, result);
            }
            writeReport(resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Format format, Node root, JarProcessor.JarFileResult<BytecodeVersions> result) {
        appendSubject(root, result.getJarPath(), "bytecode show", resultNode -> {
            BytecodeVersions bytecodeVersions = result.getResult();
            BytecodeVersion bc = BytecodeVersion.of(bytecodeVersion != null && bytecodeVersion > 43 ? bytecodeVersion : 0);
            Integer jv = javaVersion != null && javaVersion > 8 ? javaVersion : 0;

            if (bc.isEmpty() && 0 == jv) {
                Set<BytecodeVersion> manifestBytecode = bytecodeVersions.getManifestBytecode();
                if (manifestBytecode.size() > 0) {
                    Node bytecode = resultNode.array($("report.key.bytecode"));
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

            if (0 == jv) {
                Map<BytecodeVersion, List<String>> unversionedClasses = bytecodeVersions.getUnversionedClasses();
                if (bc.isEmpty()) {
                    unversionedClasses.keySet().stream()
                        .sorted()
                        .forEach(bytecodeVersion -> reportUnversioned(resultNode, unversionedClasses, bytecodeVersion));
                } else {
                    reportUnversioned(resultNode, unversionedClasses, bc);
                }
            }

            Set<Integer> javaVersions = bytecodeVersions.getJavaVersionOfVersionedClasses();
            if (0 == jv) {
                for (Integer javaVersion : javaVersions) {
                    Map<BytecodeVersion, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(javaVersion);
                    if (bc.isEmpty()) {
                        for (Map.Entry<BytecodeVersion, List<String>> entry : versionedClasses.entrySet()) {
                            reportVersioned(resultNode, versionedClasses, javaVersion, entry.getKey());
                        }
                    } else {
                        reportVersioned(resultNode, versionedClasses, javaVersion, bc);
                    }
                }
            } else {
                Map<BytecodeVersion, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(jv);
                if (bc.isEmpty()) {
                    for (Map.Entry<BytecodeVersion, List<String>> entry : versionedClasses.entrySet()) {
                        reportVersioned(resultNode, versionedClasses, jv, entry.getKey());
                    }
                } else {
                    reportVersioned(resultNode, versionedClasses, jv, bc);
                }
            }
        });
    }

    private void reportUnversioned(Node resultNode, Map<BytecodeVersion, List<String>> unversionedClasses, BytecodeVersion bytecodeVersion) {
        if (!unversionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = unversionedClasses.get(bytecodeVersion);
        Node unversioned = resultNode.node($("report.key.unversioned"))
            .node($("report.key.bytecode")).value(bytecodeVersion).end()
            .node($("report.key.total")).value(classes.size()).end();

        if (details) {
            unversioned.array($("report.key.classes"))
                .collapsableChildren($("report.key.class"), classes);
        }
    }

    private void reportVersioned(Node resultNode, Map<BytecodeVersion, List<String>> versionedClasses, Integer javaVersion, BytecodeVersion bytecodeVersion) {
        if (!versionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = versionedClasses.get(bytecodeVersion);
        Node versioned = resultNode.node($("report.key.versioned"))
            .node($("report.key.java.version")).value(javaVersion).end()
            .node($("report.key.bytecode")).value(bytecodeVersion).end()
            .node($("report.key.total")).value(classes.size()).end();

        if (details) {
            versioned.array($("report.key.classes"))
                .collapsableChildren($("report.key.class"), classes);
        }
    }
}
