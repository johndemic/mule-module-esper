/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.event.bean.BeanEventBean;
import com.espertech.esper.event.map.MapEventBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.api.callback.SourceCallback;

/**
 * <code>UpdateListener</code> implementation that invokes the <code>SourceCallback</code> facilitate the
 * dispatch of events as they are read off the event stream.
 */
public class SourceCallbackUpdateListener implements UpdateListener {

    protected transient Log logger = LogFactory.getLog(getClass());

    SourceCallback sourceCallback;

    public SourceCallbackUpdateListener(SourceCallback sourceCallback) {
        this.sourceCallback = sourceCallback;
    }

    // ToDo Figure out how to deal with newEvents vs. oldEvents intelligently
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {

        if (logger.isDebugEnabled()) {
            int newEventSize = 0;
            int oldEventSize = 0;

            if (newEvents != null) {
                newEventSize = newEvents.length;
            }

            if (oldEvents != null) {
                oldEventSize = oldEvents.length;
            }

            logger.debug(String.format("Received %d new events and %d old events", newEventSize, oldEventSize));
        }
        /*
        ToDo this should probably be made configurable, giving the user the option to return a NullPayload
        instead of suppressing null events.
         */
        if (newEvents == null) {
            logger.debug("Null events collection received");
            return;
        }

        for (EventBean event : newEvents) {
            try {
                logger.debug("Processing received event: " + event);
                sourceCallback.process(event.getUnderlying());
            } catch (Exception e) {
                logger.error("Could not process event: " + event, e);
            }
        }

    }
}
