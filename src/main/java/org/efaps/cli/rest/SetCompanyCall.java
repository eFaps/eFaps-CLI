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

import org.efaps.cli.utils.CLISettings;
import org.efaps.json.AbstractEFapsJSON;
import org.efaps.json.reply.ContextReply;
import org.efaps.json.reply.ErrorReply;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class SetCompanyCall
    extends AbstractContextCall
{

    /**
     * Instantiates a new sets the company call.
     *
     * @param _environment the _environment
     */
    public SetCompanyCall(final Environment _environment)
    {
        super(_environment, "setCompany");
    }

    /**
     * Execute.
     *
     * @return the string
     */
    public String execute()
    {
        final StringBuilder ret = new StringBuilder();
        if (init()) {
            final Response response = getWebTarget().queryParam("company",
                            (String) getEnvironment().getVariable(CLISettings.COMPANY).getValue())
                            .request(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE).get();

            if (MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {
                final BufferedReader br = new BufferedReader(new InputStreamReader(
                                response.readEntity(InputStream.class)));
                final ObjectMapper mapper = new ObjectMapper();
                try {
                    final Object obj = mapper.readValue(br, AbstractEFapsJSON.class);
                    if (obj instanceof ErrorReply) {
                        ret.append(getErrorReply(response, (ErrorReply) obj));
                    } else if (obj instanceof ContextReply) {
                        ret.append(getContextReply(response, (ContextReply) obj));
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
        }
        return ret.toString();
    }
}
