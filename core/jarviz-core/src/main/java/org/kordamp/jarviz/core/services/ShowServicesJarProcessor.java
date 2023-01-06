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
package org.kordamp.jarviz.core.services;

import org.apache.commons.io.IOUtils;
import org.kordamp.jarviz.core.JarFileResolver;
import org.kordamp.jarviz.core.JarProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.kordamp.jarviz.util.JarUtils.withJarEntry;
import static org.kordamp.jarviz.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ShowServicesJarProcessor implements JarProcessor<Optional<List<String>>> {
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
    public Optional<List<String>> getResult() {
        List<String> services = new ArrayList<>();
        boolean foundServices = false;

        String target = META_INF_SERVICES + serviceName;
        try (JarFile jarFile = jarFileResolver.resolveJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.equals(target)) {
                    foundServices = true;
                    services.addAll(withJarEntry(jarFile, entry, inputStream -> IOUtils.readLines(inputStream, StandardCharsets.UTF_8).stream()
                        .filter(s -> isNotBlank(s) && !s.startsWith("#"))
                        .collect(toList())));
                    break;
                }
            }

            return foundServices ? Optional.of(unmodifiableList(services)) : Optional.empty();
        } catch (IOException ignored) {
            return foundServices ? Optional.of(unmodifiableList(services)) : Optional.empty();
        }
    }
}
