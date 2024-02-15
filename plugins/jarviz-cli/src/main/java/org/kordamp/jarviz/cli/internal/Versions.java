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
package org.kordamp.jarviz.cli.internal;

import org.kordamp.jarviz.util.JarvizVersion;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Versions implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JarvizVersion.banner(new PrintStream(baos));
        return baos.toString().split(System.lineSeparator());
    }
}
