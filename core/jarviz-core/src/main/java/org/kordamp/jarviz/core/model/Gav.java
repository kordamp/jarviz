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
package org.kordamp.jarviz.core.model;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.core.JarvizException;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Gav {
    public static final String GROUP_ID = "groupId";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String VERSION = "version";

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;

    public Gav(String gav) {
        String[] parts = gav.split(":");
        if (parts.length == 4) {
            this.groupId = parts[0].trim().replace(".", "/");
            this.artifactId = parts[1].trim();
            this.version = parts[2].trim();
            this.classifier = parts[3].trim();
        } else if (parts.length == 3) {
            this.groupId = parts[0].trim().replace(".", "/");
            this.artifactId = parts[1].trim();
            this.version = parts[2].trim();
            this.classifier = null;
        } else {
            throw new JarvizException(RB.$("ERROR_INVALID_GAV", gav));
        }
    }

    public Gav(Properties props) {
        this.groupId = props.getProperty(GROUP_ID);
        this.artifactId = props.getProperty(ARTIFACT_ID);
        this.version = props.getProperty(VERSION);
        this.classifier = null;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getClassifier() {
        return classifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gav gav = (Gav) o;
        return groupId.equals(gav.groupId) &&
            artifactId.equals(gav.artifactId) &&
            version.equals(gav.version) &&
            Objects.equals(classifier, gav.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, classifier);
    }
}
