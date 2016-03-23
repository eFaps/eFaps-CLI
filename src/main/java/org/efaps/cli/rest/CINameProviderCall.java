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
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.efaps.eql.ui.contentassist.ICINameProvider;
import org.efaps.json.AbstractEFapsJSON;
import org.efaps.json.data.DataList;
import org.efaps.json.data.ObjectData;
import org.efaps.json.reply.ErrorReply;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class CINameProviderCall
    extends AbstractCall
{

    /**
     * Instantiates a new CI name provider call.
     *
     * @param _environment the _environment
     */
    public CINameProviderCall(final Environment _environment)
    {
        super(_environment, "eql", "print");
    }

    /**
     * Gets the CI name provider.
     *
     * @return the CI name provider
     */
    public ICINameProvider execute()
    {
        ICINameProvider ret = null;
        try {
            init();
            final String eql = "print query type Admin_DataModel_Type select attribute[Name]";
            final Response response = getWebTarget().queryParam("origin", "eFaps-CLI").queryParam("stmt", eql)
                            .request(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE).get();
            final BufferedReader br = new BufferedReader(new InputStreamReader(response.readEntity(InputStream.class)));
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JodaModule());
            final Object obj = mapper.readValue(br, AbstractEFapsJSON.class);
            if (obj instanceof ErrorReply) {
                getErrorReply(response, (ErrorReply) obj);
            } else {
                final DataList dataList = (DataList) obj;
                final Set<String> typeNames = new HashSet<>();
                for (final ObjectData data : dataList) {
                    typeNames.add((String) data.getValues().get(0).getValue());
                }
                ret = new ICINameProvider()
                {
                    @Override
                    public Set<String> getTypeNames()
                    {
                        return typeNames;
                    }
                };
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }
}
