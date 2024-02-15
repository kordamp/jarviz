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

import org.kordamp.jarviz.util.Algorithm;

import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Checksum implements Comparable<Checksum> {
    private final Algorithm algorithm;
    private final Outcome outcome;

    public static Checksum success(Algorithm algorithm) {
        return new Checksum(algorithm, Outcome.SUCCESS);
    }

    public static Checksum failure(Algorithm algorithm) {
        return new Checksum(algorithm, Outcome.FAILURE);
    }

    public static Checksum unavailable(Algorithm algorithm) {
        return new Checksum(algorithm, Outcome.UNAVAILABLE);
    }

    private Checksum(Algorithm algorithm, Outcome outcome) {
        this.algorithm = algorithm;
        this.outcome = outcome;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Checksum checksum = (Checksum) o;
        return algorithm == checksum.algorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm);
    }

    @Override
    public int compareTo(Checksum o) {
        if (null == o) return -1;
        return algorithm.compareTo(o.algorithm);
    }

    public enum Outcome {
        SUCCESS,
        FAILURE,
        UNAVAILABLE
    }
}
