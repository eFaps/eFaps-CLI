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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.cli.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.efaps.cli.utils.CLISettings;
import org.efaps.json.data.AbstractValue;
import org.efaps.json.data.DataList;
import org.efaps.json.data.DateTimeValue;
import org.efaps.json.data.DecimalValue;
import org.efaps.json.data.LongValue;
import org.efaps.json.data.ObjectData;
import org.efaps.json.data.StringValue;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import com.brsanthu.dataexporter.model.Column;
import com.brsanthu.dataexporter.model.NumberColumn;
import com.brsanthu.dataexporter.model.Row;
import com.brsanthu.dataexporter.model.StringColumn;
import com.brsanthu.dataexporter.output.texttable.TextTableExporter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RestClient
{

    /**
     * Client that makes the actual connection.
     */
    private Client client;

    /**
     * Url to be used in the request by this client.
     */
    private WebTarget webTarget;

    private final Environment environment;

    private boolean initialized;

    /**
     * @param _url url for this client.
     */
    public RestClient(final Environment _environment)
    {
        this.environment = _environment;
    }

    /**
     * Initialize the client.
     */
    public void init()
    {
        if (!this.initialized) {
            final String pwd = (String) this.environment.getVariable(CLISettings.PWD).getValue();
            final String user = (String) this.environment.getVariable(CLISettings.USER).getValue();
            final String url = (String) this.environment.getVariable(CLISettings.URL).getValue();
            final ClientConfig clientConfig = new ClientConfig();

            final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user, pwd);
            clientConfig.register(feature).register(MultiPartFeature.class);

            this.client = ClientBuilder.newClient(clientConfig);
            this.webTarget = this.client.target(url);
        }
    }

    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     */
    public String compile(final String _target)
    {
        init();
        final WebTarget resourceWebTarget = this.webTarget.path("compile");

        final Response response = resourceWebTarget.queryParam("type", _target).request(MediaType.TEXT_PLAIN_TYPE)
                        .get();
        final String ret;
        if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret = response.readEntity(String.class);
        } else {
            ret = response.getStatusInfo().toString();
        }
        return ret;
    }

    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     */
    public String update(final String _eql)
    {
        init();

        final WebTarget resourceWebTarget = this.webTarget.path("eql").path("update");
        final Response response = resourceWebTarget.queryParam("origin", "eFaps-CLI")
                        .queryParam("stmt", _eql)
                        .request(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE).get();
        final String ret;
        if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret = response.readEntity(String.class);
        } else {
            ret = response.getStatusInfo().toString();
        }
        return ret;
    }

    /**
     * Compile the target in the server.
     *
     * @param _target target to be compiled
     */
    public String print(final String _eql)
    {
        init();
        final StringBuilder ret = new StringBuilder();
        final WebTarget resourceWebTarget = this.webTarget.path("eql").path("print");
        final Response response = resourceWebTarget.queryParam("origin", "eFaps-CLI")
                        .queryParam("stmt", _eql)
                        .request(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE).get();

        if (MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {

            final BufferedReader br = new BufferedReader(new InputStreamReader(response.readEntity(InputStream.class)));

            final ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            mapper.registerModule(new JodaModule());

            try {
                final StringWriter writer = new StringWriter();
                final TextTableExporter tableWriter = new TextTableExporter(writer);

                final DataList tmp = mapper.readValue(br, DataList.class);
                final Map<String, Column> key2Column = new LinkedHashMap<>();
                for (final ObjectData objData : tmp) {
                    for (final AbstractValue<?> val : objData.getValues()) {
                        int length = 0;
                        if (key2Column.containsKey(val.getKey())) {
                            length = String.valueOf(val.getValue()).length() + 2;
                        } else {
                            if (val instanceof StringValue) {
                                key2Column.put(val.getKey(), new StringColumn(val.getKey()));
                            } else if (val instanceof DateTimeValue) {
                                key2Column.put(val.getKey(), new StringColumn(val.getKey()));
                            } else if (val instanceof DecimalValue) {
                                key2Column.put(val.getKey(), new NumberColumn(val.getKey(), 1, 2));
                            } else if (val instanceof LongValue) {
                                key2Column.put(val.getKey(), new NumberColumn(val.getKey(), 1, 0));
                            } else {
                                key2Column.put(val.getKey(), new StringColumn(val.getKey()));
                            }
                            length = val.getKey().length() > String.valueOf(val.getValue()).length() ? val
                                            .getKey().length() :
                                            String.valueOf(val.getValue()).length();
                        }
                        if (length > key2Column.get(val.getKey()).getWidth()) {
                            key2Column.get(val.getKey()).setWidth(length);
                        }
                    }
                }
                tableWriter.addColumns(key2Column.values().toArray(new Column[key2Column.size()]));

                for (final ObjectData objData : tmp) {
                    final Row row = new Row();
                    for (final AbstractValue<?> val : objData.getValues()) {
                        row.addCellValue(val.getValue());
                    }
                    tableWriter.addRows(row);
                }
                ret.append(writer);
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (MediaType.TEXT_PLAIN_TYPE.equals(response.getMediaType())) {
            ret.append(response.readEntity(String.class));
        } else {
            ret.append(response.getStatusInfo().toString());
        }
        return ret.toString();
    }

    /**
     * @param _files
     * @param _revFile
     * @throws Exception
     */
    public void post(final List<File> _files,
                     final File _revFile)
        throws Exception
    {

        final Map<String, String[]> fileInfo = new HashMap<>();
        if (_revFile != null) {
            final BufferedReader br = new BufferedReader(new FileReader(_revFile));
            String line;
            while ((line = br.readLine()) != null) {
                final String[] arr = line.split(" ");
                if (arr.length > 2) {
                    fileInfo.put(arr[0], new String[] { arr[1], arr[2] });
                }
            }
            br.close();
        }

        final FormDataMultiPart multiPart = new FormDataMultiPart();
        for (final File file : _files) {
            final FileDataBodyPart part = new FileDataBodyPart("eFaps_File", file);
            multiPart.bodyPart(part);
            if (_revFile == null) {
                /*
                 * final String[] info = getFileInformation(file);
                 * multiPart.field("eFaps_Revision", info[0]);
                 * multiPart.field("eFaps_Date", info[1]);
                 */
            } else {
                final String[] info = fileInfo.get(file.getName());
                if (info == null) {
                    multiPart.field("eFaps_Revision", "");
                    multiPart.field("eFaps_Date", "");
                } else {
                    multiPart.field("eFaps_Revision", info[0]);
                    multiPart.field("eFaps_Date", info[1]);
                }
            }
        }
        this.webTarget.path("update").request()
                        .post(Entity.entity(multiPart, multiPart.getMediaType()));
    }
}
