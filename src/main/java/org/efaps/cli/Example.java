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

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.annotation.Command;
import de.raysha.lib.jsimpleshell.annotation.Param;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class Example
{

    public static void main(final String[] args)
        throws IOException
    {
        final Shell shell = ShellBuilder.shell("eFaps")
                        .behavior()
                        .addHandler(new Example())
                        .addHandler(new MessageResolver())
                        .build();
        shell.setAppName("\"eFaps Command Line Interface\"");
        shell.commandLoop();
    }

    @Command(abbrev = "add")
    public Long addition(final Integer... integers)
    {
        Long result = 0L;
        for (final Integer i : integers) {
            result += i;
        }

        return result;
    }

    @Command(name = "division", abbrev = "div")
    public Double div(
                      @Param("dividend") final Double a,
                      @Param("divisor") final Double b)
    {

        return a / b;
    }

}
