/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 The Jarviz authors.
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

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "show")
public class BytecodeShow extends AbstractJarvizSubcommand<Bytecode> {
    @CommandLine.Option(names = {"--details"})
    public boolean details;

    @Override
    protected int execute() {
        ShowBytecodeJarProcessor processor = null != exclusive.file ?
            new ShowBytecodeJarProcessor(exclusive.file) :
            new ShowBytecodeJarProcessor(resolveOutputDirectory(), exclusive.url);

        BytecodeVersions bytecodeVersions = processor.getResult();

        Set<Integer> manifestBytecode = bytecodeVersions.getManifestBytecode();
        if (manifestBytecode.size() > 0) {
            parent().getOut().println(RB.$("bytecode.version.attribute", manifestBytecode.stream()
                .map(String::valueOf)
                .collect(joining(","))));
        }

        Map<Integer, List<String>> unversionedClasses = bytecodeVersions.getUnversionedClasses();
        if (unversionedClasses.size() > 0) {
            unversionedClasses.keySet().stream().sorted().forEach(bytecodeVersion -> {
                List<String> classes = unversionedClasses.get(bytecodeVersion);
                parent().getOut().println(RB.$("bytecode.unversioned.classes.total",
                    bytecodeVersion, classes.size()));
                if (details) {
                    classes.forEach(parent().getOut()::println);
                }
            });
        }

        Set<Integer> javaVersions = bytecodeVersions.getJavaVersionOfVersionedClasses();
        if (javaVersions.size() > 0) {
            for (Integer javaVersion : javaVersions) {
                Map<Integer, List<String>> versionedClasses = bytecodeVersions.getVersionedClasses(javaVersion);
                if (versionedClasses.size() > 0) {
                    for (Map.Entry<Integer, List<String>> entry : versionedClasses.entrySet()) {
                        parent().getOut().println(RB.$("bytecode.versioned.classes.total",
                            javaVersion, entry.getKey(), entry.getValue().size()));
                        if (details) {
                            entry.getValue().forEach(parent().getOut()::println);
                        }
                    }
                }
            }
        }

        return 0;
    }
}
