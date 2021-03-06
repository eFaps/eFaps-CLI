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

import de.raysha.lib.jsimpleshell.handler.CommandLoopObserver;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class EQLObserver
    implements CommandLoopObserver
{

    /** The instance. */
    private static EQLObserver INSTANCE = new EQLObserver();

    /** The eql. */
    private StringBuilder eql = new StringBuilder();

    /**
     * Singelton.
     */
    private EQLObserver()
    {
    }

    @Override
    public void cliBeforeCommandLine(final String _line)
    {
        this.eql.append(_line);
    }

    @Override
    public void cliAfterCommandLine(final String _line)
    {
        this.eql = new StringBuilder();
    }

    /**
     * This method is called when information about an EQL
     * which was previously requested using an asynchronous
     * interface becomes available.
     *
     * @return the EQL observer
     */
    public static EQLObserver get()
    {
        return INSTANCE;
    }

    /**
     * Getter method for the instance variable {@link #eql}.
     *
     * @return value of instance variable {@link #eql}
     */
    public StringBuilder getEql()
    {
        return this.eql;
    }
}
