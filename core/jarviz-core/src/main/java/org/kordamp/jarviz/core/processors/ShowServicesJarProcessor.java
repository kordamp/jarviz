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

import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;
import org.kordamp.jarviz.core.JarvizException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.kordamp.jarviz.util.JarUtils.withJarEntry;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ShowServicesJarProcessor implements JarProcessor<Optional<Set<String>>> {
    private static final String META_INF_SERVICES = "META-INF/services/";
    private final JarFileResolver jarFileResolver;
    private Integer release;
    private String serviceName;

    public ShowServicesJarProcessor(JarFileResolver jarFileResolver) {
        this.jarFileResolver = jarFileResolver;
    }

    public Integer getRelease() {
        return release;
    }

    public void setRelease(Integer release) {
        this.release = release;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public Set<JarFileResult<Optional<Set<String>>>> getResult() throws JarvizException {
        Set<JarFileResult<Optional<Set<String>>>> set = new TreeSet<>();

        for (JarFile jarFile : jarFileResolver.resolveJarFiles()) {
            set.add(processJarFile(jarFile));
        }

        return set;
    }

    private JarFileResult<Optional<Set<String>>> processJarFile(JarFile jarFile) {
        Set<String> services = new TreeSet<>();
        boolean foundServices = false;

        String target = META_INF_SERVICES + serviceName;
        try (jarFile) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.equals(target)) {
                    foundServices = true;
                    services.addAll(withJarEntry(jarFile, entry, inputStream -> new BufferedReader(new InputStreamReader(inputStream,
                        StandardCharsets.UTF_8)).lines()
                        .filter(s -> isNotBlank(s) && !s.startsWith("#"))
                        .collect(toSet())));
                    break;
                }
            }

            return JarFileResult.of(jarFile, foundServices ? Optional.of(unmodifiableSet(services)) : Optional.empty());
        } catch (IOException ignored) {
            return JarFileResult.of(jarFile, foundServices ? Optional.of(unmodifiableSet(services)) : Optional.empty());
        }
    }
}
