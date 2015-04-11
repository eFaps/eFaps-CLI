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

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.annotation.Command;
import de.raysha.lib.jsimpleshell.annotation.Inject;
import de.raysha.lib.jsimpleshell.annotation.Param;
import de.raysha.lib.jsimpleshell.io.InputBuilder;
import de.raysha.lib.jsimpleshell.io.TerminalIO;

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

    @Command
    public void update(@Param("StatementParts") final String... _parts)
        throws IOException
    {
        final StringBuilder eql = new StringBuilder().append("update ");
        for (final String part : _parts) {
            eql.append(part).append(" ");
        }
        while (!StringUtils.endsWithAny(eql, ";", "; ")) {
            eql.append(this.input.in().withPromt("\\").readLine());
        }
        final Collection<Object> col = this.owner.getSettings().getAuxHandlers().get("!");
        if (!col.isEmpty()) {
            final TerminalIO io = (TerminalIO) col.iterator().next();
            final History history = io.getConsole().getHistory();
            history.removeLast();
            history.previous();
            history.add(eql);
        }
    }
}
