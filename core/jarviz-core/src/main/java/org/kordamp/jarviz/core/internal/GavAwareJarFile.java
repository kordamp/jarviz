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
package org.kordamp.jarviz.core.internal;

import org.kordamp.jarviz.core.model.Gav;
import org.kordamp.jarviz.core.model.GavAware;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class GavAwareJarFile extends JarFile implements GavAware {
    private final Gav gav;

    public GavAwareJarFile(File file, Gav gav) throws IOException {
        super(file);
        this.gav = gav;
    }

    @Override
    public Gav getGav() {
        return gav;
    }
}
