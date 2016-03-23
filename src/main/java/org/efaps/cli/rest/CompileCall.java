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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class CompileCall extends AbstractCall
{

    /**
     * Instantiates a new compile call.
     *
     * @param _environment the _environment
     */
    public CompileCall(final Environment _environment)
    {
        super(_environment, "compile");
    }
    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     * @return the string
     */
    public String execute(final String _target)
    {
        init();
        final Response response = getWebTarget().queryParam("type", _target).request(MediaType.TEXT_PLAIN_TYPE)
                        .get();
        final String ret;
        if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret = response.readEntity(String.class);
        } else {
            ret = response.getStatusInfo().toString();
        }
        return ret;
    }
}
