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

import org.efaps.cli.utils.Util;
import org.efaps.json.reply.InsertEQLReply;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class InsertCall
    extends AbstractModifyCall
{

    /**
     * Instantiates a new update call.
     *
     * @param _environment the _environment
     */
    public InsertCall(final Environment _environment)
    {
        super(_environment, "insert");
    }

    @Override
    protected CharSequence getReply(final Response _response,
                                    final Object _obj)
    {
        final StringBuilder ret = new StringBuilder();
        if (_obj instanceof InsertEQLReply) {
            ret.append(MessageFormat.format(Util.getBundle().getString(InsertCall.class.getName() + ".Reply"),
                            _response.getStatusInfo(), ((InsertEQLReply) _obj).getModified(),
                            ((InsertEQLReply) _obj).getInstance()));
        }
        return ret;
    }
}
