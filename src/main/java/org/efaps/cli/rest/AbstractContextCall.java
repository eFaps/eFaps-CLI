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


package org.efaps.cli.rest;

import java.text.MessageFormat;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.cli.utils.Util;
import org.efaps.json.reply.ContextReply;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractContextCall
    extends AbstractCall
{

    /**
     * Instantiates a new abstract context call.
     *
     * @param _environment the _environment
     * @param _paths the _paths
     */
    public AbstractContextCall(final Environment _environment,
                                  final String... _paths)
    {
        super(_environment, ArrayUtils.add(_paths, 0, "context"));
    }

    /**
     * Gets the error reply.
     *
     * @param _response the _response
     * @param _contextReply the _context reply
     * @return the error reply
     */
    protected CharSequence getContextReply(final Response _response,
                                           final ContextReply _contextReply)
    {
        return MessageFormat.format(Util.getBundle().getString(
                        AbstractContextCall.class.getName() + ".Reply"),
                        _response.getStatusInfo(),
                        _contextReply.getUserName(),
                        _contextReply.getUserFirstName(),
                        _contextReply.getUserLastName(),
                        _contextReply.getCompanyName(),
                        _contextReply.getLocale());
    }
}
