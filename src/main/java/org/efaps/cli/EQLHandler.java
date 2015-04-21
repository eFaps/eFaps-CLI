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

import jline.console.history.History;

import org.apache.commons.lang3.StringUtils;
import org.efaps.cli.rest.RestClient;

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.annotation.Command;
import de.raysha.lib.jsimpleshell.annotation.Inject;
import de.raysha.lib.jsimpleshell.annotation.Param;
import de.raysha.lib.jsimpleshell.io.InputBuilder;
import de.raysha.lib.jsimpleshell.io.OutputBuilder;
import de.raysha.lib.jsimpleshell.io.TerminalIO;
import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class EQLHandler
{

    @Inject
    private Shell owner;

    @Inject
    private InputBuilder input;

    @Inject
    private OutputBuilder output;

    @Inject
    private Environment environment;

    @Command
    public String update(@Param("StatementParts") final String... _parts)
        throws IOException
    {
        String ret = null;
        final String stmt = getStmt();
        ret = new RestClient(this.environment).update(stmt);
        history(stmt);
        return ret;
    }

    @Command
    public String print(@Param("StatementParts") final String... _parts)
        throws IOException
    {
        String ret = null;
        final String stmt = getStmt();
        ret = new RestClient(this.environment).print(stmt);
        history(stmt);
        return ret;
    }

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

    protected String getStmt()
        throws IOException
    {
        final StringBuilder eql = EQLObserver.get().getEql();
        while (!StringUtils.endsWithAny(eql, ";", "; ")) {
            eql.append(this.input.in().withPromt("\\").readLine());
        }
        return StringUtils.removeEnd(StringUtils.strip(eql.toString()), ";");
    }
}
