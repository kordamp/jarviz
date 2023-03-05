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

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
module org.kordamp.jarviz.cli {
    exports org.kordamp.jarviz.cli;

    requires org.kordamp.jarviz.core;
    requires info.picocli;

    exports org.kordamp.jarviz.cli.bytecode to info.picocli;
    exports org.kordamp.jarviz.cli.checksum to info.picocli;
    exports org.kordamp.jarviz.cli.entries to info.picocli;
    exports org.kordamp.jarviz.cli.internal to info.picocli;
    exports org.kordamp.jarviz.cli.manifest to info.picocli;
    exports org.kordamp.jarviz.cli.modules to info.picocli;
    exports org.kordamp.jarviz.cli.packages to info.picocli;
    exports org.kordamp.jarviz.cli.services to info.picocli;
    opens org.kordamp.jarviz.cli.internal to info.picocli;
}