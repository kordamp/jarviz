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
package org.kordamp.jarviz.commands;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.Format;
import org.kordamp.jarviz.core.internal.AbstractCommand;
import org.kordamp.jarviz.core.internal.AbstractConfiguration;
import org.kordamp.jarviz.core.internal.Colorizer;
import org.kordamp.jarviz.core.model.BytecodeVersion;
import org.kordamp.jarviz.core.model.BytecodeVersions;
import org.kordamp.jarviz.core.processors.BytecodeShowJarProcessor;
import org.kordamp.jarviz.core.processors.JarProcessor;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;
import org.kordamp.jarviz.reporting.Node;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class BytecodeShowCommand extends AbstractCommand<BytecodeShowCommand.Configuration> {
    public static Configuration config() {
        return new Configuration();
    }

    public static class Configuration extends AbstractConfiguration<Configuration> {
        private boolean details;
        private Integer bytecodeVersion;
        private Integer javaVersion;

        public boolean isDetails() {
            return details;
        }

        public Configuration withDetails(boolean details) {
            this.details = details;
            return this;
        }

        public Integer getBytecodeVersion() {
            return bytecodeVersion;
        }

        public Configuration withBytecodeVersion(Integer bytecodeVersion) {
            this.bytecodeVersion = bytecodeVersion;
            return this;
        }

        public Integer getJavaVersion() {
            return javaVersion;
        }

        public Configuration withJavaVersion(Integer javaVersion) {
            this.javaVersion = javaVersion;
            return this;
        }
    }

    @Override
    public int execute(Configuration configuration) {
        JarFileResolver jarFileResolver = createJarFileResolver(configuration);
        BytecodeShowJarProcessor processor = new BytecodeShowJarProcessor(jarFileResolver);

        Set<JarProcessor.JarFileResult<BytecodeVersions>> results = processor.getResult();
        if (results.isEmpty()) {
            return 1;
        }

        output(configuration, results);
        report(configuration, results);

        return 0;
    }

    private void output(Configuration configuration, Set<JarProcessor.JarFileResult<BytecodeVersions>> results) {
        Node root = createRootNode();
        Format outputFormat = configuration.getOutputFormat();
        for (JarProcessor.JarFileResult<BytecodeVersions> result : results) {
            if (null == outputFormat) {
                output(configuration, result);
            } else {
                buildReport(configuration, outputFormat, root, result);
            }
        }
        if (null != outputFormat) writeOutput(configuration, resolveFormatter(outputFormat).write(root));
    }

    private void output(Configuration configuration, JarProcessor.JarFileResult<BytecodeVersions> result) {
        configuration.getOut().println($$("output.subject", result.getJarFileName()));
        BytecodeVersions bytecodeVersions = result.getResult();

        BytecodeVersion bc = BytecodeVersion.of(configuration.getBytecodeVersion() != null && configuration.getBytecodeVersion() > 43 ? configuration.getBytecodeVersion() : 0);
        Integer jv = configuration.getJavaVersion() != null && configuration.getJavaVersion() > 8 ? configuration.getJavaVersion() : 0;

        if (bc.isEmpty() && 0 == jv) {
            Set<BytecodeVersion> manifestBytecode = bytecodeVersions.getManifestBytecode();
            if (manifestBytecode.size() > 0) {
                configuration.getOut().println($$("bytecode.version.attribute", manifestBytecode.stream()
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
                    .forEach(bytecodeVersion -> printUnversioned(configuration, unversionedClasses, bytecodeVersion));
            } else {
                printUnversioned(configuration, unversionedClasses, bc);
            }
        }

        Set<Integer> javaVersions = bytecodeVersions.getJavaVersionOfVersionedClasses();
        if (0 == jv) {
            for (Integer javaVersion : javaVersions) {
                Map<BytecodeVersion, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(javaVersion);
                if (bc.isEmpty()) {
                    for (Map.Entry<BytecodeVersion, List<String>> entry : versionedClasses.entrySet()) {
                        printVersioned(configuration, versionedClasses, javaVersion, entry.getKey());
                    }
                } else {
                    printVersioned(configuration, versionedClasses, javaVersion, bc);
                }
            }
        } else {
            Map<BytecodeVersion, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(jv);
            if (bc.isEmpty()) {
                for (Map.Entry<BytecodeVersion, List<String>> entry : versionedClasses.entrySet()) {
                    printVersioned(configuration, versionedClasses, jv, entry.getKey());
                }
            } else {
                printVersioned(configuration, versionedClasses, jv, bc);
            }
        }
    }

    private void printUnversioned(Configuration configuration, Map<BytecodeVersion, List<String>> unversionedClasses, BytecodeVersion bytecodeVersion) {
        if (!unversionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = unversionedClasses.get(bytecodeVersion);
        configuration.getOut().println($$("bytecode.unversioned.classes.total", bytecodeVersion, classes.size()));
        if (configuration.isDetails()) {
            classes.forEach(configuration.getOut()::println);
        }
    }

    private void printVersioned(Configuration configuration, Map<BytecodeVersion, List<String>> versionedClasses, Integer javaVersion, BytecodeVersion bytecodeVersion) {
        if (!versionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = versionedClasses.get(bytecodeVersion);
        configuration.getOut().println($$("bytecode.versioned.classes.total", javaVersion, bytecodeVersion, classes.size()));
        if (configuration.isDetails()) {
            classes.forEach(configuration.getOut()::println);
        }
    }

    private void report(Configuration configuration, Set<JarProcessor.JarFileResult<BytecodeVersions>> results) {
        if (null == configuration.getReportPath()) return;

        for (Format format : configuration.getReportFormats()) {
            Node root = createRootNode();
            for (JarProcessor.JarFileResult<BytecodeVersions> result : results) {
                buildReport(configuration, format, root, result);
            }
            writeReport(configuration, resolveFormatter(format).write(root), format);
        }
    }

    private void buildReport(Configuration configuration, Format format, Node root, JarProcessor.JarFileResult<BytecodeVersions> result) {
        appendSubject(root, result.getJarPath(), "bytecode show", resultNode -> {
            BytecodeVersions bytecodeVersions = result.getResult();

            BytecodeVersion bc = BytecodeVersion.of(configuration.getBytecodeVersion() != null && configuration.getBytecodeVersion() > 43 ? configuration.getBytecodeVersion() : 0);
            Integer jv = configuration.getJavaVersion() != null && configuration.getBytecodeVersion() > 8 ? configuration.getBytecodeVersion() : 0;

            if (bc.isEmpty() && 0 == jv) {
                Set<BytecodeVersion> manifestBytecode = bytecodeVersions.getManifestBytecode();
                if (manifestBytecode.size() > 0) {
                    Node bytecode = resultNode.array(RB.$("report.key.bytecode"));
                    manifestBytecode.stream()
                        .map(String::valueOf)
                        .forEach(v -> {
                            if (format == Format.TXT) {
                                bytecode.node(v).end();
                            } else {
                                bytecode.collapsable(RB.$("report.key.version")).value(v).end();
                            }
                        });
                }
            }

            if (0 == jv) {
                Map<BytecodeVersion, List<String>> unversionedClasses = bytecodeVersions.getUnversionedClasses();
                if (bc.isEmpty()) {
                    unversionedClasses.keySet().stream()
                        .sorted()
                        .forEach(bytecodeVersion -> reportUnversioned(configuration, resultNode, unversionedClasses, bytecodeVersion));
                } else {
                    reportUnversioned(configuration, resultNode, unversionedClasses, bc);
                }
            }

            Set<Integer> javaVersions = bytecodeVersions.getJavaVersionOfVersionedClasses();
            if (0 == jv) {
                for (Integer javaVersion : javaVersions) {
                    Map<BytecodeVersion, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(javaVersion);
                    if (bc.isEmpty()) {
                        for (Map.Entry<BytecodeVersion, List<String>> entry : versionedClasses.entrySet()) {
                            reportVersioned(configuration, resultNode, versionedClasses, javaVersion, entry.getKey());
                        }
                    } else {
                        reportVersioned(configuration, resultNode, versionedClasses, javaVersion, bc);
                    }
                }
            } else {
                Map<BytecodeVersion, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(jv);
                if (bc.isEmpty()) {
                    for (Map.Entry<BytecodeVersion, List<String>> entry : versionedClasses.entrySet()) {
                        reportVersioned(configuration, resultNode, versionedClasses, jv, entry.getKey());
                    }
                } else {
                    reportVersioned(configuration, resultNode, versionedClasses, jv, bc);
                }
            }
        });
    }

    private void reportUnversioned(Configuration configuration, Node resultNode, Map<BytecodeVersion, List<String>> unversionedClasses, BytecodeVersion bytecodeVersion) {
        if (!unversionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = unversionedClasses.get(bytecodeVersion);
        Node unversioned = resultNode.node(RB.$("report.key.unversioned"))
            .node(RB.$("report.key.bytecode")).value(bytecodeVersion).end()
            .node(RB.$("report.key.total")).value(classes.size()).end();

        if (configuration.isDetails()) {
            unversioned.array(RB.$("report.key.classes"))
                .collapsableChildren(RB.$("report.key.class"), classes);
        }
    }

    private void reportVersioned(Configuration configuration, Node resultNode, Map<BytecodeVersion, List<String>> versionedClasses, Integer javaVersion, BytecodeVersion bytecodeVersion) {
        if (!versionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = versionedClasses.get(bytecodeVersion);
        Node versioned = resultNode.node(RB.$("report.key.versioned"))
            .node(RB.$("report.key.java.version")).value(javaVersion).end()
            .node(RB.$("report.key.bytecode")).value(bytecodeVersion).end()
            .node(RB.$("report.key.total")).value(classes.size()).end();

        if (configuration.isDetails()) {
            versioned.array(RB.$("report.key.classes"))
                .collapsableChildren(RB.$("report.key.class"), classes);
        }
    }
}
