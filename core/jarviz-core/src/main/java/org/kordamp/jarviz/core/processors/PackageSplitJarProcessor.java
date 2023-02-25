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
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class PackageSplitJarProcessor implements JarProcessor<Set<String>> {
    private static final String MODULE_INFO = "module-info.class";
    private static final String VERSIONED = "META-INF/versions/";

    private final JarFileResolver jarFileResolver;

    public PackageSplitJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    @Override
    public Set<JarFileResult<Set<String>>> getResult() throws JarvizException {
        Set<JarFileResult<Set<String>>> set = new TreeSet<>();

        PackageCounter packageCounter = new PackageCounter();
        PackageTracker packageTracker = new PackageTracker();

        Set<JarFile> jarFiles = jarFileResolver.resolveJarFiles();
        if (jarFiles.size() < 2) {
            throw new JarvizException(RB.$("ERROR_NOT_ENOUGH_INPUT_JARS", "2"));
        }

        for (JarFile jarFile : jarFiles) {
            try (jarFile) {
                jarFile.stream()
                    .map(JarEntry::getName)
                    .filter(entryName -> entryName.endsWith(".class") && !entryName.startsWith(VERSIONED) && !entryName.equals(MODULE_INFO))
                    .map(this::asPackage)
                    .distinct()
                    .sorted()
                    .forEach(thePackage -> {
                        packageCounter.add(thePackage);
                        packageTracker.add(jarFile, thePackage);
                    });
            } catch (IOException e) {
                throw new JarvizException(RB.$("ERROR_OPENING_JAR", jarFile.getName()));
            }
        }

        // remove all packages with count == 1
        packageCounter.cleanup();
        // retain split packages
        packageTracker.cleanup(packageCounter.packages.keySet());
        for (Map.Entry<JarFile, Set<String>> e : packageTracker.packages.entrySet()) {
            Set<String> splitPackages = e.getValue();
            if (!splitPackages.isEmpty()) {
                set.add(JarFileResult.of(e.getKey(), new TreeSet<>(splitPackages)));
            }
        }

        return set;
    }

    private String asPackage(String name) {
        int i = name.lastIndexOf('/');
        return i != -1 ? name.substring(0, i).replace('/', '.') : "";
    }

    private static class PackageCounter {
        private static final Integer ZERO = 0;
        private final Map<String, Integer> packages = new LinkedHashMap<>();

        private void add(String thePackage) {
            if (isNotBlank(thePackage)) {
                Integer count = packages.computeIfAbsent(thePackage, k -> ZERO);
                packages.put(thePackage, count + 1);
            }
        }

        private void cleanup() {
            for (String key : new TreeSet<>(packages.keySet())) {
                if (packages.get(key) == 1) {
                    packages.remove(key);
                }
            }
        }
    }

    private static class PackageTracker {
        private final Map<JarFile, Set<String>> packages = new LinkedHashMap<>();

        private void add(JarFile jarFile, String thePackage) {
            if (isNotBlank(thePackage)) {
                packages.computeIfAbsent(jarFile, k -> new TreeSet<>())
                    .add(thePackage);
            }
        }

        private void cleanup(Set<String> splitPackages) {
            for (Set<String> packagerPerJar : packages.values()) {
                packagerPerJar.retainAll(splitPackages);
            }
        }
    }
}
