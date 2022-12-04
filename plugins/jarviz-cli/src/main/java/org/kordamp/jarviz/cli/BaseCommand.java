/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2022 The Jarviz authors.
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
package org.kordamp.jarviz.cli;

import org.slf4j.helpers.MessageFormatter;
import picocli.CommandLine;

import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(mixinStandardHelpOptions = true,
    versionProvider = Versions.class,
    resourceBundle = "org.kordamp.jarviz.cli.Messages")
abstract class BaseCommand {
    @CommandLine.Spec
    public CommandLine.Model.CommandSpec spec;

    public ResourceBundle bundle = ResourceBundle.getBundle("org.kordamp.jarviz.cli.Messages");

    @CommandLine.Option(names = "-D",
        paramLabel = "<key=value>",
        descriptionKey = "system-property",
        mapFallbackValue = "")
    void setProperty(Map<String, String> props) {
        props.forEach(System::setProperty);
    }

    protected String $(String key, Object... args) {
        if (null == args || args.length == 0) {
            return bundle.getString(key);
        }
        return MessageFormatter.arrayFormat(bundle.getString(key), args).getMessage();
    }
}
