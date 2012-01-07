/**
 * Mule Development Kit
 * Copyright 2010-2011 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
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
 */

/**
 * This file was automatically generated by the Mule Development Kit
 */
package org.mule.module.esper;

import com.espertech.esper.client.*;
import com.espertech.esper.event.map.MapEventBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.ConnectionException;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.annotations.*;
import org.mule.api.annotations.param.Optional;
import org.mule.api.annotations.param.Payload;
import org.mule.api.callback.SourceCallback;
import org.mule.api.context.MuleContextAware;

import java.net.URL;

/**
 * Cloud Connector for Esper.
 *
 * @author MuleSoft, Inc.
 */
@Connector(name = "esper", schemaVersion = "1.0")
public class EsperConnector implements MuleContextAware {

    protected transient Log logger = LogFactory.getLog(getClass());

    private EPServiceProvider esperServiceProvider;

    private MuleContext muleContext;
    /**
     * The optional location of an Esper config file.
     */
    @Configurable
    @Optional
    private String configuration;


    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }


    public void setMuleContext(MuleContext muleContext) {
        this.muleContext = muleContext;
    }


    @Connect
    public void connect() throws ConnectionException {
        Configuration c = new Configuration();

        if (StringUtils.isNotBlank(configuration)) {
            logger.debug("Initializing EsperServiceProvider with configuration: " + configuration);
            URL configURL = Thread.currentThread().getContextClassLoader().getResource(configuration);
            c.configure(configURL);
        }
        esperServiceProvider = EPServiceProviderManager.getDefaultProvider(c);
    }

    @Disconnect
    public void disconnect() {
        logger.debug("Destroying EsperServiceProvider");
        esperServiceProvider.destroy();
    }

    @ConnectionIdentifier
    public String connectionId() {
        return esperServiceProvider.getURI();
    }

    @ValidateConnection
    public boolean isConnected() {
        return esperServiceProvider != null;
    }

    /**
     * Sends events to an Esper event stream.
     * <p/>
     * {@sample.xml ../../../doc/Esper-connector.xml.sample esper:send-event}
     *
     * @param eventPayload The event to be injected into the event stream.
     */
    @Processor
    public void send(Object eventPayload) {
        logger.debug(String.format("Sending event %s to stream", eventPayload));
        esperServiceProvider.getEPRuntime().sendEvent(eventPayload);
    }

    /**
     * Listens for events matching the specified query statement.
     * <p/>
     * {@sample.xml ../../../doc/Esper-connector.xml.sample esper:listen}
     *
     * @param statement The Esper statement to select events from a stream.
     * @param callback  The callback to be called when a message is received
     */
    @Source
    public void listen(String statement, final SourceCallback callback) {
        logger.debug("Listening for events with statement: " + statement);
        EPStatement s = esperServiceProvider.getEPAdministrator().createEPL(statement);
        s.addListener(new SourceCallbackUpdateListener(callback));
    }

    /**
     * Utility transformer to extract a property from an <code>EventBean</code>.
     * <p/>
     * {@sample.xml ../../../doc/Esper-connector.xml.sample esper:get-event-property}
     *
     * @param event The <code>EventBean</code> to transform.
     * @param key   The property to extract
     * @return The object corresponding to the parameter key
     */
    @Processor
    public Object getEventProperty(@Payload EventBean event, String key) {
        return event.get(key);
    }

    /**
     * Utility transformer to extract the properties from a <code>MapEventBean</code>.
     * <p/>
     * {@sample.xml ../../../doc/Esper-connector.xml.sample esper:get-map-event-properties}
     *
     * @param event The <code>MapEventBean</code> to transform.
     * @return The event's properties
     */
    @Processor
    public Object getMapEventProperties(@Payload MapEventBean event) {
        return event.getProperties();
    }
}
