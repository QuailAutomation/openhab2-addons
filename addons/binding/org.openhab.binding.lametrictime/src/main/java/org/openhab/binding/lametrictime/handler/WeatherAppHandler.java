/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.handler;

import static org.openhab.binding.lametrictime.LaMetricTimeBindingConstants.CHANNEL_APP_COMMAND;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.lametrictime.config.LaMetricTimeAppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.syphr.lametrictime.api.local.ApplicationActionException;
import org.syphr.lametrictime.api.model.CoreApps;

/**
 * The {@link WeatherAppHandler} represents an instance of the built-in weather app.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class WeatherAppHandler extends AbstractLaMetricTimeAppHandler {
    private static final String PACKAGE_NAME = "com.lametric.weather";

    public static final String COMMAND_FORECAST = "forecast";

    private final Logger logger = LoggerFactory.getLogger(WeatherAppHandler.class);

    public WeatherAppHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleAppCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_APP_COMMAND:
                    handleCommandChannel(command);
                    updateActiveAppOnDevice();
                    updateState(channelUID, new StringType()); // clear state
                    break;
                default:
                    logger.debug("Channel '{}' not supported", channelUID);
                    break;
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Failed to perform action - taking app offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected String getPackageName(LaMetricTimeAppConfiguration config) {
        return PACKAGE_NAME;
    }

    private void handleCommandChannel(Command command) throws ApplicationActionException {
        String commandStr = command.toFullString();
        switch (commandStr) {
            case COMMAND_FORECAST:
                getDevice().doAction(getWidget(), CoreApps.weather().forecast());
                break;
            default:
                logger.debug("Weather app command '{}' not supported", commandStr);
                break;
        }
    }
}
