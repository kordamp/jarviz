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
package org.kordamp.jarviz.core.model;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class ModuleName {
    public static final String EXPLICIT = "explicit";
    public static final String FILENAME = "filename";
    public static final String MANIFEST = "manifest";

    private final String moduleName;
    private final boolean automaticByManifest;
    private final boolean automaticByFilename;
    private final boolean valid;
    private final String reason;

    private ModuleName(String moduleName, boolean automaticByManifest, boolean automaticByFilename, String reason) {
        this.moduleName = moduleName;
        this.automaticByManifest = automaticByManifest;
        this.automaticByFilename = automaticByFilename;
        this.valid = null == reason;
        this.reason = reason;
    }

    public String resolveSource() {
        if (isAutomaticByManifest()) return MANIFEST;
        if (isAutomaticByFilename()) return FILENAME;
        return EXPLICIT;
    }

    public boolean isAutomatic() {
        return automaticByManifest || automaticByFilename;
    }

    public String getModuleName() {
        return moduleName;
    }

    public boolean isAutomaticByManifest() {
        return automaticByManifest;
    }

    public boolean isAutomaticByFilename() {
        return automaticByFilename;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isNotValid() {
        return !valid;
    }

    public String getReason() {
        return reason;
    }

    public String asError() {
        return moduleName + " (" + resolveSource() + ") is not valid because " + reason;
    }

    public static ModuleName fromAutomaticByManifest(String moduleName, String reason) {
        return new ModuleName(moduleName, true, false, reason);
    }

    public static ModuleName fromAutomaticByFilename(String moduleName, String reason) {
        return new ModuleName(moduleName, false, true, reason);
    }

    public static ModuleName fromModuleDescriptor(String moduleName, String reason) {
        return fromModuleDescriptor(moduleName, false, false, reason);
    }

    public static ModuleName fromModuleDescriptor(String moduleName, boolean automaticByManifest, boolean automaticByFilename, String reason) {
        return new ModuleName(moduleName, automaticByManifest, automaticByFilename, reason);
    }
}
