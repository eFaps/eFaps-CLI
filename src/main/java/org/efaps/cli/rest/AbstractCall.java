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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.cli.utils.CLISettings;
import org.efaps.cli.utils.Util;
import org.efaps.json.reply.ErrorReply;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.raysha.lib.jsimpleshell.script.Environment;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractCall
{
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCall.class);

    /**
     * Client that makes the actual connection.
     */
    private Client client;

    /**
     * Url to be used in the request by this client.
     */
    private WebTarget webTarget;

    /** The initialized. */
    private boolean initialized;

    /** The environment. */
    private final Environment environment;

    /** The paths. */
    private final String[] paths;

    /**
     * Instantiates a new rest client.
     *
     * @param _environment the _environment
     * @param _paths the _paths
     */
    public AbstractCall(final Environment _environment,
                        final String... _paths)
    {
        this.environment = _environment;
        this.paths = _paths;
    }

    /**
     * Initialize the client.
     *
     * @return true, if successful
     */
    protected boolean init()
    {
        boolean ret = false;
        if (!this.initialized) {
            final String pwd = (String) this.environment.getVariable(CLISettings.PWD).getValue();
            final String user = (String) this.environment.getVariable(CLISettings.USER).getValue();
            final String url = (String) this.environment.getVariable(CLISettings.URL).getValue();
            final ClientConfig clientConfig = new ClientConfig();

            final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(user, pwd);
            clientConfig.register(feature).register(MultiPartFeature.class);

            this.client = ClientBuilder.newClient(clientConfig);
            this.webTarget = this.client.target(url);
            if (!ArrayUtils.isEmpty(this.paths)) {
                for (final String path : this.paths) {
                    this.webTarget = this.webTarget.path(path);
                }
            }
            LOG.debug("Initialize call. for {} with {}", this.client, this.webTarget);
            ret = !StringUtils.isEmpty(pwd) && !StringUtils.isEmpty(user) && !StringUtils.isEmpty(url);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #webTarget}.
     *
     * @return value of instance variable {@link #webTarget}
     */
    protected WebTarget getWebTarget()
    {
        return this.webTarget;
    }

    /**
     * Gets the error reply.
     *
     * @param _response the _response
     * @param _errorReply the _error reply
     * @return the error reply
     */
    protected CharSequence getErrorReply(final Response _response,
                                         final ErrorReply _errorReply)
    {
        return MessageFormat.format(Util.getBundle().getString(AbstractCall.class.getName() + ".ErrorReply"),
                        _response.getStatusInfo(), _errorReply.getError(), _errorReply.getMessage(),
                        _errorReply.getStacktrace() == null ? "" :  _errorReply.getStacktrace());
    }

    /**
     * Getter method for the instance variable {@link #environment}.
     *
     * @return value of instance variable {@link #environment}
     */
    protected Environment getEnvironment()
    {
        return this.environment;
    }
}
