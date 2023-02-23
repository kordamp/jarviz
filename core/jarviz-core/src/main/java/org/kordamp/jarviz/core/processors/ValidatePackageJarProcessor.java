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
package org.kordamp.jarviz.core.processors;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.util.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.kordamp.jarviz.util.StringUtils.isBlank;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class ValidatePackageJarProcessor implements JarProcessor<Set<String>> {
    private static final String VERSIONED = "META-INF/versions/";

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
        "abstract",
        "assert",
        "boolean",
        "break",
        "byte",
        "case",
        "catch",
        "char",
        "class",
        "const",
        "continue",
        "default",
        "do",
        "double",
        "else",
        "enum",
        "extends",
        "final",
        "finally",
        "float",
        "for",
        "goto",
        "if",
        "implements",
        "import",
        "instanceof",
        "int",
        "interface",
        "long",
        "native",
        "new",
        "package",
        "private",
        "protected",
        "public",
        "return",
        "short",
        "static",
        "strictfp",
        "super",
        "switch",
        "synchronized",
        "this",
        "throw",
        "throws",
        "transient",
        "try",
        "void",
        "volatile",
        "while",
        "true",
        "false",
        "null",
        "_");

    private final JarFileResolver jarFileResolver;

    public ValidatePackageJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    @Override
    public Set<JarFileResult<Set<String>>> getResult() throws JarvizException {
        Set<JarFileResult<Set<String>>> set = new TreeSet<>();

        PackageTracker packageTracker = new PackageTracker();

        for (JarFile jarFile : jarFileResolver.resolveJarFiles()) {
            try (jarFile) {
                jarFile.stream()
                    .map(JarEntry::getName)
                    .filter(entryName -> entryName.endsWith(".class"))
                    .map(this::asPackage)
                    .distinct()
                    .filter(StringUtils::isNotBlank)
                    .filter(this::isInvalid)
                    .forEach(thePackage -> packageTracker.add(jarFile, thePackage));
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_OPENING_JAR", jarFile.getName()));
            }
        }

        for (Map.Entry<JarFile, Set<String>> e : packageTracker.packages.entrySet()) {
            Set<String> splitPackages = e.getValue();
            if (!splitPackages.isEmpty()) {
                set.add(JarFileResult.of(e.getKey(), new TreeSet<>(splitPackages)));
            }
        }

        return set;
    }

    private String asPackage(String name) {
        if (name.startsWith(VERSIONED)) {
            name = name.substring(VERSIONED.length());
            int p = name.indexOf("/");
            name = name.substring(p + 1);
        }
        int i = name.lastIndexOf('/');
        return i != -1 ? name.substring(0, i).replace('/', '.') : "";
    }

    private boolean isInvalid(String thePackage) {
        for (String part : thePackage.split("\\.")) {
            if (!isJavaIdentifier(part)) {
                return true;
            }
        }
        return false;
    }

    private static class PackageTracker {
        private final Map<JarFile, Set<String>> packages = new LinkedHashMap<>();

        private void add(JarFile jarFile, String thePackage) {
            if (isNotBlank(thePackage)) {
                packages.computeIfAbsent(jarFile, k -> new TreeSet<>())
                    .add(thePackage);
            }
        }
    }

    private boolean isJavaIdentifier(String str) {
        if (isBlank(str) || RESERVED_KEYWORDS.contains(str)) {
            return false;
        }

        int first = Character.codePointAt(str, 0);
        if (!Character.isJavaIdentifierStart(first)) {
            return false;
        }

        int i = Character.charCount(first);
        while (i < str.length()) {
            int cp = Character.codePointAt(str, i);
            if (!Character.isJavaIdentifierPart(cp)) {
                return false;
            }
            i += Character.charCount(cp);
        }

        return true;
    }
}
