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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class AbstractConfiguration<S extends AbstractConfiguration<S>> implements Configuration<S> {
    private PrintWriter out = new PrintWriter(System.out, true);
    private PrintWriter err = new PrintWriter(System.err, true);
    private boolean failOnError;
    private final Set<String> gavs = new TreeSet<>();
    private final Set<Path> files = new TreeSet<>();
    private final Set<URL> urls = new LinkedHashSet<>();
    private final Set<String> classpaths = new TreeSet<>();
    private final Set<Path> directories = new TreeSet<>();
    private Path cacheDirectory;
    private Path reportPath;
    private final Set<Format> reportFormats = new TreeSet<>();
    private Format outputFormat;

    protected S self() {
        return (S) this;
    }

    @Override
    public PrintWriter getOut() {
        return out;
    }

    @Override
    public S withOut(PrintWriter out) {
        this.out = out;
        return self();
    }

    @Override
    public PrintWriter getErr() {
        return err;
    }

    @Override
    public S withErr(PrintWriter err) {
        this.err = err;
        return self();
    }

    @Override
    public boolean isFailOnError() {
        return failOnError;
    }

    @Override
    public S withFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return self();
    }

    @Override
    public Set<String> getGavs() {
        return gavs;
    }

    @Override
    public S withGav(String gav) {
        this.gavs.add(gav.trim());
        return self();
    }

    @Override
    public S withGavs(Set<String> gavs) {
        this.gavs.addAll(gavs);
        return self();
    }

    @Override
    public Set<Path> getFiles() {
        return files;
    }

    @Override
    public S withFile(Path file) {
        this.files.add(file);
        return self();
    }

    @Override
    public S withFiles(Set<Path> files) {
        this.files.addAll(files);
        return self();
    }

    @Override
    public Set<URL> getUrls() {
        return urls;
    }

    @Override
    public S withUrl(URL url) {
        this.urls.add(url);
        return self();
    }

    @Override
    public S withUrls(Set<URL> urls) {
        this.urls.addAll(urls);
        return self();
    }

    @Override
    public Set<String> getClasspaths() {
        return classpaths;
    }

    @Override
    public S withClasspath(String classpath) {
        this.classpaths.add(classpath);
        return self();
    }

    @Override
    public S withClasspaths(Set<String> classpath) {
        this.classpaths.addAll(classpath);
        return self();
    }

    @Override
    public Set<Path> getDirectories() {
        return directories;
    }

    @Override
    public S withDirectories(Path directory) {
        this.directories.add(directory);
        return self();
    }

    @Override
    public S withDirectories(Set<Path> directories) {
        this.directories.addAll(directories);
        return self();
    }

    @Override
    public Path getCacheDirectory() {
        return cacheDirectory;
    }

    @Override
    public S withCacheDirectory(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        return self();
    }

    @Override
    public Path getReportPath() {
        return reportPath;
    }

    @Override
    public S withReportPath(Path reportPath) {
        this.reportPath = reportPath;
        return self();
    }

    @Override
    public Set<Format> getReportFormats() {
        return reportFormats;
    }

    @Override
    public S withReportFormat(Format reportFormat) {
        this.reportFormats.add(reportFormat);
        return self();
    }

    @Override
    public S withReportFormats(Set<Format> reportFormats) {
        this.reportFormats.addAll(reportFormats);
        return self();
    }

    @Override
    public Format getOutputFormat() {
        return outputFormat;
    }

    @Override
    public S withOutputFormat(Format outputFormat) {
        this.outputFormat = outputFormat;
        return self();
    }
}
