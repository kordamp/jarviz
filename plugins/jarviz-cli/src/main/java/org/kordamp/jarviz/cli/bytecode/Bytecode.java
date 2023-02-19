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
package org.kordamp.jarviz.cli.bytecode;

import org.kordamp.jarviz.cli.Main;
import org.kordamp.jarviz.cli.internal.AbstractJarvizCommand;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "bytecode",
    subcommands = {BytecodeShow.class})
public class Bytecode extends AbstractJarvizCommand<Main> {
    @CommandLine.Spec
    public CommandLine.Model.CommandSpec spec;

    @Override
    protected int execute() {
        spec.commandLine().usage(parent.getOut());
        return 0;
    }
}
