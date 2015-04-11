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

import org.efaps.cli.rest.RestClient;

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
    @Inject
    private Environment environment;

    @Command
    public void compile(@Param("target") final String _target)
        throws IOException
    {
        new RestClient(this.environment).compile(_target);
    }
}