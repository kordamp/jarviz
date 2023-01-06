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

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.cli.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.bytecode.ShowBytecodeJarProcessor;
import org.kordamp.jarviz.core.model.BytecodeVersions;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static org.kordamp.jarviz.core.resolvers.JarFileResolvers.createJarFileResolver;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "show")
public class BytecodeShow extends AbstractJarvizSubcommand<Bytecode> {
    @CommandLine.Option(names = {"--details"})
    public boolean details;

    @CommandLine.Option(names = {"--bytecode-version"})
    public Integer bytecodeVersion;

    @CommandLine.Option(names = {"--java-version"})
    public Integer javaVersion;

    @Override
    protected int execute() {
        ShowBytecodeJarProcessor processor = new ShowBytecodeJarProcessor(createJarFileResolver(
            exclusive.file, exclusive.gav, exclusive.url, resolveOutputDirectory()));

        BytecodeVersions bytecodeVersions = processor.getResult();

        Integer bc = bytecodeVersion != null && bytecodeVersion > 43 ? bytecodeVersion : 0;
        Integer jv = javaVersion != null && javaVersion > 8 ? javaVersion : 0;

        if (bc == 0 && jv == 0) {
            Set<Integer> manifestBytecode = bytecodeVersions.getManifestBytecode();
            if (manifestBytecode.size() > 0) {
                parent().getOut().println(RB.$("bytecode.version.attribute", manifestBytecode.stream()
                    .map(String::valueOf)
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

        return 0;
    }

    private void printUnversioned(Map<Integer, List<String>> unversionedClasses, Integer bytecodeVersion) {
        if (!unversionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = unversionedClasses.get(bytecodeVersion);
        parent().getOut().println(RB.$("bytecode.unversioned.classes.total", bytecodeVersion, classes.size()));
        if (details) {
            classes.forEach(parent().getOut()::println);
        }
    }

    private void printVersioned(Map<Integer, List<String>> versionedClasses, Integer javaVersion, Integer bytecodeVersion) {
        if (!versionedClasses.containsKey(bytecodeVersion)) return;

        List<String> classes = versionedClasses.get(bytecodeVersion);
        parent().getOut().println(RB.$("bytecode.versioned.classes.total", javaVersion, bytecodeVersion, classes.size()));
        if (details) {
            classes.forEach(parent().getOut()::println);
        }
    }
}
