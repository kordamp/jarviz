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

import java.io.PrintWriter;
import java.util.concurrent.Callable;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractCommand<C extends IO> implements Callable<Integer>, IO {
    protected abstract C parent();

    @Override
    public PrintWriter getOut() {
        return parent().getOut();
    }

    @Override
    public void setOut(PrintWriter out) {
        parent().setOut(out);
    }

    @Override
    public PrintWriter getErr() {
        return parent().getErr();
    }

    @Override
    public void setErr(PrintWriter err) {
        parent().setErr(err);
    }

    public Integer call() {
        setup();

        try {
            return execute();
        } catch (Exception e) {
            e.printStackTrace(parent().getOut());
            return 1;
        }
    }

    protected void setup() {
        Banner.display(parent().getOut());

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
    }

    protected abstract int execute();
}
