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
package org.kordamp.jarviz.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class BytecodeVersions {
    private final Set<Integer> manifestBytecode = new TreeSet<>();
    private final Map<Integer, List<String>> unversionedClasses = new LinkedHashMap<>();
    private final Map<Integer, Map<Integer, List<String>>> versionedClasses = new LinkedHashMap<>();

    public Set<Integer> getManifestBytecode() {
        return unmodifiableSet(manifestBytecode);
    }

    public void setManifestBytecode(Set<Integer> manifestBytecode) {
        this.manifestBytecode.clear();
        this.manifestBytecode.addAll(manifestBytecode);
    }

    public void addUnversionedClass(Integer bytecodeVersion, String className) {
        unversionedClasses.computeIfAbsent(bytecodeVersion, k -> new ArrayList<>())
            .add(className);
    }

    public void addVersionedClass(Integer javaVersion, Integer bytecodeVersion, String className) {
        versionedClasses.computeIfAbsent(javaVersion, k -> new LinkedHashMap<>())
            .computeIfAbsent(bytecodeVersion, k -> new ArrayList<>())
            .add(className);
    }

    public Set<Integer> getBytecodeOfUnversionedClasses() {
        return unmodifiableSet(new TreeSet<>(unversionedClasses.keySet()));
    }

    public Map<Integer, List<String>> getUnversionedClasses() {
        LinkedHashMap<Integer, List<String>> tmp = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<String>> entry : unversionedClasses.entrySet()) {
            tmp.put(entry.getKey(), unmodifiableList(entry.getValue()));
        }
        return unmodifiableMap(tmp);
    }

    public Set<Integer> getJavaVersionOfVersionedClasses() {
        return unmodifiableSet(new TreeSet<>(versionedClasses.keySet()));
    }

    public Map<Integer, List<String>> getVersionedClasses(Integer javaVersion) {
        if (!versionedClasses.containsKey(javaVersion)) return emptyMap();

        LinkedHashMap<Integer, List<String>> tmp = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<String>> entry : versionedClasses.get(javaVersion).entrySet()) {
            tmp.put(entry.getKey(), unmodifiableList(entry.getValue()));
        }
        return unmodifiableMap(tmp);
    }
}
