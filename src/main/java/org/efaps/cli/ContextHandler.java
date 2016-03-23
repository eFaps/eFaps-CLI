/*
 * Copyright 2003 - 2016 The eFaps Team
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

import org.efaps.cli.rest.ConfirmContextCall;
import org.efaps.cli.rest.SetCompanyCall;
import org.efaps.cli.utils.CLISettings;
import org.efaps.cli.utils.ExportFormat;
import org.efaps.cli.utils.Util;

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

    /** The testcompany. */
    public static final String FAKECOMPANY = ContextHandler.class.getName() + ".FAKECOMPANY";

    /** The context. */
    private static ContextHandler CONTEXT;

    /** The owner. */
    @Inject
    private Shell owner;

    /** The input. */
    @Inject
    private InputBuilder input;

    /**
     * Singelton wanted.
     */
    private ContextHandler()
    {
    }

    /**
     * Login.
     *
     * @param _username the username
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String login(@Param("username") final String _username)
        throws IOException
    {
        this.owner.getEnvironment().setVariable(CLISettings.USER, _username);
        final String passwd = this.input.maskedIn('*').withPromt(
                        Util.getBundle().getString(ContextHandler.class.getName() + ".PasswdChallenge")).readLine();
        this.owner.getEnvironment().setVariable(CLISettings.PWD, passwd);
        return new ConfirmContextCall(this.owner.getEnvironment()).execute();
    }

    /**
     * Url.
     *
     * @param _url the url
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String url(@Param("url") final String _url)
        throws IOException
    {
        this.owner.getEnvironment().setVariable(CLISettings.URL, _url);
        return new ConfirmContextCall(this.owner.getEnvironment()).execute();
    }

    /**
     * Set the Company.
     *
     * @param _company the _company
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String company(@Param("company") final String _company)
        throws IOException
    {
        String ret = null;
        if (FAKECOMPANY.equals(_company)) {
            ret = new ConfirmContextCall(this.owner.getEnvironment()).execute();
        } else {
            this.owner.getEnvironment().setVariable(CLISettings.COMPANY, _company);
            ret = new SetCompanyCall(this.owner.getEnvironment()).execute();
        }
        return ret;
    }

    /**
     * Export format.
     *
     * @param _format the format
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public void exportFormat(@Param("ExportFormat") final ExportFormat _format)
        throws IOException
    {
        exportFormat(_format, "export");
    }

    /**
     * Export format.
     *
     * @param _format the format
     * @param _fileName the _file name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public void exportFormat(@Param("ExportFormat") final ExportFormat _format,
                             @Param("FileName") final String _fileName)
        throws IOException
    {
        this.owner.getEnvironment().setVariable(CLISettings.EXPORTFORMAT, _format);
        this.owner.getEnvironment().setVariable(CLISettings.FILENAME, _fileName);
    }

    /**
     * Help shortcut.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public void help()
        throws IOException
    {
        this.owner.getPipeline().append("?");
    }

    /**
     * Gets the.
     *
     * @return the context handler
     */
    public static ContextHandler get()
    {
        if (ContextHandler.CONTEXT == null) {
            ContextHandler.CONTEXT = new ContextHandler();
        }
        return ContextHandler.CONTEXT;
    }
}
