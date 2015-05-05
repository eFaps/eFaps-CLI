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

import java.io.IOException;

import org.efaps.cli.utils.CLISettings;
import org.efaps.cli.utils.ExportFormat;

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.annotation.Command;
import de.raysha.lib.jsimpleshell.annotation.Inject;
import de.raysha.lib.jsimpleshell.annotation.Param;
import de.raysha.lib.jsimpleshell.io.InputBuilder;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class ContextHandler
{

    private static ContextHandler CONTEXT;

    @Inject
    private Shell owner;
    @Inject
    private InputBuilder input;

    /**
     * Singelton wanted.
     */
    private ContextHandler()
    {
    }

    @Command
    public void login(@Param("username") final String _username)
        throws IOException
    {
        this.owner.getEnvironment().setVariable(CLISettings.USER, _username);
        final String passwd = this.input.maskedIn('*').withPromt("Enter your password please: ").readLine();
        this.owner.getEnvironment().setVariable(CLISettings.PWD, passwd);
    }

    @Command
    public void url(@Param("url") final String _url)
        throws IOException
    {
        this.owner.getEnvironment().setVariable(CLISettings.URL, _url);
    }

    @Command
    public void exportFormat(@Param("ExportFormat") final ExportFormat _format)
        throws IOException
    {
        this.owner.getEnvironment().setVariable(CLISettings.EXPORTFORMAT, _format);
    }

    public static ContextHandler get()
    {
        if (ContextHandler.CONTEXT == null) {
            ContextHandler.CONTEXT = new ContextHandler();
        }
        return ContextHandler.CONTEXT;
    }
}
