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

import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.resolvers.JarFileResolver;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableSet;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class EntriesFindJarProcessor implements JarProcessor<Set<String>> {
    private final JarFileResolver jarFileResolver;
    private String entryName;
    private String entryPattern;

    public EntriesFindJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getEntryPattern() {
        return entryPattern;
    }

    public EntriesFindJarProcessor setEntryPattern(String entryPattern) {
        this.entryPattern = entryPattern;
        return this;
    }

    @Override
    public Set<JarFileResult<Set<String>>> getResult() throws JarvizException {
        Set<JarFileResult<Set<String>>> set = new TreeSet<>();

        Pattern pattern = isNotBlank(entryPattern) ? Pattern.compile(entryPattern) : null;
        for (JarFile jarFile : jarFileResolver.resolveJarFiles()) {
            set.add(processJarFile(jarFile, pattern));
        }

        return set;
    }

    private JarFileResult<Set<String>> processJarFile(JarFile jarFile, Pattern pattern) {
        Set<String> entries = new TreeSet<>();

        try (jarFile) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();
                String name = entry.getName();
                if (null != pattern) {
                    if (pattern.matcher(name).matches()) {
                        entries.add(name);
                    }
                } else if (name.equals(entryName)) {
                    entries.add(name);
                    break;
                }
            }

            return JarFileResult.of(jarFile, unmodifiableSet(entries));
        } catch (IOException ignored) {
            return JarFileResult.of(jarFile, unmodifiableSet(entries));
        }
    }
}
