/*
 * Copyright 2003 - 2015 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.efaps.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.efaps.cli.rest.CompileCall;
import org.efaps.cli.rest.ImportCICall;

import de.raysha.lib.jsimpleshell.annotation.Command;
import de.raysha.lib.jsimpleshell.annotation.Inject;
import de.raysha.lib.jsimpleshell.annotation.Param;
import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class CommandHandler
{

    /** The environment. */
    @Inject
    private Environment environment;

    /**
     * Compile.
     *
     * @param _target the _target
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String compile(@Param("target") final String _target)
        throws IOException
    {
        return new CompileCall(this.environment).execute(_target);
    }

    /**
     * Import ci.
     *
     * @param _files the _files
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String importCI(@Param("files") final File... _files)
        throws IOException
    {
        final List<File> files = new ArrayList<>();
        File revFile = null;
        for (final File file : _files) {
            if ("_revFile.txt".equals(file.getName())) {
                revFile = file;
            } else {
                files.add(file);
            }
        }
        return new ImportCICall(this.environment).execute(files, revFile);
    }

}
