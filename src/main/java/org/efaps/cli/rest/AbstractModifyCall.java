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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.json.AbstractEFapsJSON;
import org.efaps.json.reply.ErrorReply;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractModifyCall
    extends AbstractCall
{

    /**
     * Instantiates a new abstract modify call.
     *
     * @param _environment the _environment
     * @param _paths the _paths
     */
    public AbstractModifyCall(final Environment _environment,
                              final String... _paths)
    {
        super(_environment, ArrayUtils.add(_paths, 0, "eql"));
    }

    /**
     * Execute.
     *
     * @param _eql the _eql
     * @return the string
     */
    public String execute(final String _eql)
    {
        init();
        final Response response = getWebTarget().queryParam("origin", "eFaps-CLI")
                        .queryParam("stmt", _eql)
                        .request(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE).get();
        final StringBuilder ret = new StringBuilder();
        if (MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(response.readEntity(InputStream.class)));
            final ObjectMapper mapper = new ObjectMapper();
            try {
                final Object obj = mapper.readValue(br, AbstractEFapsJSON.class);
                if (obj instanceof ErrorReply) {
                    ret.append(getErrorReply(response, (ErrorReply) obj));
                } else {
                    ret.append(getReply(response, obj));
                }
            } catch (final IOException e) {
                ret.append(response.getStatusInfo().toString()).append(e);
            }
        } else if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret.append(response.readEntity(String.class));
        }
        if (ret.length() == 0) {
            ret.append(response.getStatusInfo().toString());
        }
        return ret.toString();
    }

    /**
     * Gets the reply.
     *
     * @param _response the _response
     * @param _obj the _obj
     * @return the reply
     */
    protected abstract CharSequence getReply(final Response _response,
                                              final Object _obj);

}
