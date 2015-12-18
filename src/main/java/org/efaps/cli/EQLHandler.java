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
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.efaps.cli.rest.RestClient;
import org.efaps.cli.utils.CLISettings;
import org.efaps.cli.utils.ExportFormat;

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.annotation.Command;
import de.raysha.lib.jsimpleshell.annotation.Inject;
import de.raysha.lib.jsimpleshell.annotation.Param;
import de.raysha.lib.jsimpleshell.io.InputBuilder;
import de.raysha.lib.jsimpleshell.io.OutputBuilder;
import de.raysha.lib.jsimpleshell.io.TerminalIO;
import de.raysha.lib.jsimpleshell.script.Environment;
import jline.console.history.History;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class EQLHandler
{

    /** The owner. */
    @Inject
    private Shell owner;

    /** The input. */
    @Inject
    private InputBuilder input;

    /** The output. */
    @Inject
    private OutputBuilder output;

    /** The environment. */
    @Inject
    private Environment environment;

    /**
     * Update.
     *
     * @param _parts the parts
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String update(@Param(value = "StatementParts", type = "eql") final String... _parts)
        throws IOException
    {
        String ret = null;
        final String stmt = getStmt();
        ret = new RestClient(this.environment).update(stmt);
        history(stmt + ";");
        return ret;
    }

    /**
     * Prints the.
     *
     * @param _parts the parts
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String print(@Param(value = "StatementParts", type = "eql") final String... _parts)
        throws IOException
    {
        String ret = null;
        final String stmt = getStmt();
        ret = new RestClient(this.environment).print(stmt, this.environment.existsVariable(CLISettings.EXPORTFORMAT)
                        ? (ExportFormat) this.environment.getVariable(CLISettings.EXPORTFORMAT).getValue()
                                        : ExportFormat.CONSOLE,
                        this.environment.existsVariable(CLISettings.FILENAME)
                                ? (String) this.environment.getVariable(CLISettings.FILENAME).getValue()
                                : null);
        history(stmt + ";");
        return ret;
    }

    /**
     * Insert.
     *
     * @param _parts the parts
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String insert(@Param(value = "StatementParts", type = "eql") final String... _parts)
        throws IOException
    {
        String ret = null;
        final String stmt = getStmt();
        ret = new RestClient(this.environment).insert(stmt);
        history(stmt + ";");
        return ret;
    }

    /**
     * Delete.
     *
     * @param _parts the parts
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Command
    public String delete(@Param(value = "StatementParts", type = "eql") final String... _parts)
        throws IOException
    {
        String ret = null;
        final String stmt = getStmt();
        ret = new RestClient(this.environment).delete(stmt);
        history(stmt + ";");
        return ret;
    }

    /**
     * History.
     *
     * @param _stmt the stmt
     */
    protected void history(final String _stmt)
    {
        final Collection<Object> col = this.owner.getSettings().getAuxHandlers().get("!");
        if (!col.isEmpty()) {
            final TerminalIO io = (TerminalIO) col.iterator().next();
            final History history = io.getConsole().getHistory();
            history.removeLast();
            history.previous();
            history.add(_stmt);
        }
    }

    /**
     * Gets the stmt.
     *
     * @return the stmt
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected String getStmt()
        throws IOException
    {
        final StringBuilder eql = EQLObserver.get().getEql();
        while (!StringUtils.endsWithAny(eql, ";", "; ", ";  ", ";   ")) {
            eql.append(this.input.in().withPromt("\\").readLine());
        }
        return StringUtils.removeEnd(StringUtils.strip(eql.toString()), ";");
    }
}
