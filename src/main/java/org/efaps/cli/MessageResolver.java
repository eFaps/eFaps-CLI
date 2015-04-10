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

import java.util.Locale;

import de.raysha.lib.jsimpleshell.handler.impl.AbstractMessageResolver;
import de.raysha.lib.jsimpleshell.handler.impl.DefaultMessageResolver;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class MessageResolver
    extends AbstractMessageResolver
{

    @Override
    public boolean supportsLocale(final Locale locale)
    {
        return true;
    }

    @Override
    protected String resolveMessage(final String _msg)
    {

        String ret;
        if ("command.abbrev.helpdetail".equals(_msg)) {
            ret = "help";
        } else {
            ret = DefaultMessageResolver.getInstance().resolveGeneralMessage(_msg);
        }
        return ret;
    }
}
