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

import org.kordamp.jarviz.core.Format;

import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public interface Configuration<S extends Configuration<S>> {
    PrintWriter getOut();

    S withOut(PrintWriter out);

    PrintWriter getErr();

    S withErr(PrintWriter err);

    boolean isFailOnError();

    S withFailOnError(boolean failOnError);

    Set<String> getGavs();

    S withGav(String gav);

    S withGavs(Set<String> gavs);

    Set<Path> getFiles();

    S withFile(Path file);

    S withFiles(Set<Path> files);

    Set<URL> getUrls();

    S withUrl(URL url);

    S withUrls(Set<URL> urls);

    Set<String> getClasspaths();

    S withClasspath(String classpath);

    S withClasspaths(Set<String> classpath);

    Set<Path> getDirectories();

    S withDirectories(Path directory);

    S withDirectories(Set<Path> directories);

    Path getCacheDirectory();

    S withCacheDirectory(Path cacheDirectory);

    Path getReportPath();

    S withReportPath(Path reportPath);

    Set<Format> getReportFormats();

    S withReportFormat(Format reportFormat);

    S withReportFormats(Set<Format> reportFormats);

    Format getOutputFormat();

    S withOutputFormat(Format outputFormat);
}
