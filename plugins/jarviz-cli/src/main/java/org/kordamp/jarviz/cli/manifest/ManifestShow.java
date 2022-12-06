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
package org.kordamp.jarviz.cli.manifest;

import org.kordamp.jarviz.bundle.RB;
import org.kordamp.jarviz.cli.AbstractJarvizSubcommand;
import org.kordamp.jarviz.core.JarvizException;
import org.kordamp.jarviz.core.processors.ShowManifestJarProcessor;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "show")
public class ManifestShow extends AbstractJarvizSubcommand<Manifest> {
    @Override
    protected int execute() {
        ShowManifestJarProcessor processor = null != exclusive.file ?
            new ShowManifestJarProcessor(exclusive.file) :
            new ShowManifestJarProcessor(resolveOutputDirectory(), exclusive.url);

        java.util.jar.Manifest manifest = processor.getResult();
        if (null == manifest) return 1;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            manifest.write(baos);
            baos.flush();
            baos.close();
            parent().getOut().println(baos);
        } catch (IOException e) {
            throw new JarvizException(RB.$("ERROR_UNEXPECTED_WRITE"), e);
        }

        return 0;
    }
}
