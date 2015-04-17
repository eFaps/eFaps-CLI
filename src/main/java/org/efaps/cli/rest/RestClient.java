/*
 * Copyright 2003 - 2010 The eFaps Team
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.efaps.cli.utils.CLISettings;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

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

        final Response ret = resourceWebTarget.queryParam("type", _target).request(MediaType.TEXT_PLAIN_TYPE)
                        .get();
        return ret.getStatusInfo().toString();
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
        final Response ret = resourceWebTarget.queryParam("origin", "eFaps-CLI")
                        .queryParam("stmt", _eql)
                        .request(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE).get();
        return ret.getStatusInfo().toString();
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
                /*final String[] info = getFileInformation(file);
                multiPart.field("eFaps_Revision", info[0]);
                multiPart.field("eFaps_Date", info[1]);*/
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
