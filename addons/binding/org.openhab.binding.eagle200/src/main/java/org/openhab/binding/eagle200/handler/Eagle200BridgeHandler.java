/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.eagle200.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.eagle200.Eagle200BindingConstants;
import org.openhab.binding.eagle200.discovery.EagleDiscoveryService;
import org.openhab.binding.eagle200.internal.Eagle200Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Eagle200BridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@NonNullByDefault
public class Eagle200BridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(Eagle200BridgeHandler.class);

    @Nullable
    private Eagle200Configuration config;

    private Eagle200Connection connection;

    private EagleDiscoveryService discoveryService;

    @SuppressWarnings("null")
    public Eagle200BridgeHandler(Bridge bridge) {
        super(bridge);
        this.connection = new Eagle200Connection(this);
    }

    @Override
    public void initialize() {
        config = getConfigAs(Eagle200Configuration.class);

        if (!config.isComplete()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        this.connection.updateConfiguration(config);
        updateStatus(ThingStatus.ONLINE);
        this.discoveryService.activate();
    }

    public Eagle200Configuration getConfiguration() {
        return getConfigAs(Eagle200Configuration.class);
    }

    public Eagle200Connection getConnection() {
        return this.connection;
    }

    @Override
    public void handleConfigurationUpdate(@NonNull Map<@NonNull String, @NonNull Object> update) {

        super.handleConfigurationUpdate(update);

        Configuration config = this.editConfiguration();
        if (update.containsKey(Eagle200BindingConstants.THING_BRIDGECONFIG_HOSTNAME)) {
            config.put(Eagle200BindingConstants.THING_BRIDGECONFIG_HOSTNAME,
                    update.get(Eagle200BindingConstants.THING_BRIDGECONFIG_HOSTNAME));
        }
        if (update.containsKey(Eagle200BindingConstants.THING_BRIDGECONFIG_CLOUDID)) {
            config.put(Eagle200BindingConstants.THING_BRIDGECONFIG_CLOUDID,
                    update.get(Eagle200BindingConstants.THING_BRIDGECONFIG_CLOUDID));
        }
        if (update.containsKey(Eagle200BindingConstants.THING_BRIDGECONFIG_INSTALLCODE)) {
            config.put(Eagle200BindingConstants.THING_BRIDGECONFIG_INSTALLCODE,
                    update.get(Eagle200BindingConstants.THING_BRIDGECONFIG_INSTALLCODE));
        }
        ThingStatus currentStatus = this.getThing().getStatus();
        this.updateConfiguration(config);
        if (!this.getConfigAs(Eagle200Configuration.class).isComplete()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        if (!currentStatus.equals(ThingStatus.ONLINE)) {
            this.connection.updateConfiguration(this.getConfigAs(Eagle200Configuration.class));
            this.updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("eagle200 bridge handleCommand called");
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void registerDiscoveryService(EagleDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @SuppressWarnings("null")
    public void unregisterDiscoveryService() {
        this.discoveryService = null;
    }
}
