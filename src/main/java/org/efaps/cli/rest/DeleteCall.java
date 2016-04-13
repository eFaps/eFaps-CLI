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
import org.efaps.json.reply.DeleteEQLReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class DeleteCall
    extends AbstractModifyCall
{
    /** The Constant LOG. */
    private final static Logger LOG = LoggerFactory.getLogger(DeleteCall.class);

    /**
     * Instantiates a new update call.
     *
     * @param _environment the _environment
     */
    public DeleteCall(final Environment _environment)
    {
        super(_environment, "delete");
    }

    @Override
    protected CharSequence getReply(final Response _response,
                                    final Object _obj)
    {
        final StringBuilder ret = new StringBuilder();
        if (_obj instanceof DeleteEQLReply) {
            LOG.debug("Recieved reply {}", _obj);
            ret.append(MessageFormat.format(Util.getBundle().getString(DeleteCall.class.getName() + ".Reply"),
                            _response.getStatusInfo(), ((DeleteEQLReply) _obj).getModified()));
        }
        return ret;
    }
}
