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
package org.kordamp.jarviz.cli;

import org.kordamp.jarviz.cli.bytecode.Bytecode;
import org.kordamp.jarviz.cli.internal.Banner;
import org.kordamp.jarviz.cli.internal.BaseCommand;
import org.kordamp.jarviz.cli.manifest.Manifest;
import org.kordamp.jarviz.cli.services.Services;
import picocli.AutoComplete;
import picocli.CommandLine;

import java.io.PrintWriter;

import static java.util.ResourceBundle.getBundle;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "jarviz",
    subcommands = {
        Bytecode.class, Manifest.class, org.kordamp.jarviz.cli.modules.Module.class, Services.class,
        AutoComplete.GenerateCompletion.class})
public class Main extends BaseCommand implements Runnable, IO {
    private PrintWriter out;
    private PrintWriter err;

    @Override
    public PrintWriter getOut() {
        return out;
    }

    @Override
    public void setOut(PrintWriter out) {
        this.out = out;
    }

    @Override
    public PrintWriter getErr() {
        return err;
    }

    @Override
    public void setErr(PrintWriter err) {
        this.err = err;
    }

    public void run() {
        Banner.display(err);

        spec.commandLine().usage(out);
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String... args) {
        Main cmd = new Main();
        CommandLine commandLine = new CommandLine(cmd);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.setUsageHelpWidth(90);
        commandLine.setUsageHelpLongOptionsMaxWidth(42);
        commandLine.setResourceBundle(getBundle("org.kordamp.jarviz.cli.internal.Messages"));
        cmd.out = commandLine.getOut();
        cmd.err = commandLine.getErr();
        return execute(commandLine, args);
    }

    public static int run(PrintWriter out, PrintWriter err, String... args) {
        Main cmd = new Main();
        CommandLine commandLine = new CommandLine(cmd);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.setUsageHelpWidth(90);
        commandLine.setUsageHelpLongOptionsMaxWidth(42);
        commandLine.setResourceBundle(getBundle("org.kordamp.jarviz.cli.internal.Messages"));
        commandLine.setOut(out);
        commandLine.setErr(err);
        cmd.out = out;
        cmd.err = err;
        return execute(commandLine, args);
    }

    private static int execute(CommandLine commandLine, String[] args) {
        return commandLine.execute(args);
    }
}
