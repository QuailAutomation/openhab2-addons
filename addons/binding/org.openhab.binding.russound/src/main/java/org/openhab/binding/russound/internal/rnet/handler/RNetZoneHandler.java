/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet.handler;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.controller.RioControllerHandler;
import org.openhab.binding.russound.internal.rnet.ChannelStateUpdate;
import org.openhab.binding.russound.internal.rnet.RNetConstants;
import org.openhab.binding.russound.internal.rnet.RNetProtocolCommands;
import org.openhab.binding.russound.internal.rnet.RnetZoneConfig;
import org.openhab.binding.russound.internal.rnet.ZoneId;
import org.openhab.binding.russound.internal.rnet.RNetProtocolCommands.ZoneCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for a RNet Russound Zone. A zone is the main receiving area for music.
 *
 * @author Craig Hamilton
 */
public class RNetZoneHandler extends BaseThingHandler {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(RNetZoneHandler.class);

    /**
     * The zone we are attached to
     */
    private ZoneId id;
    private long lastRefreshRequest = 0;
    private Collection<ChannelStateUpdate> lastUpdate;

    /**
     * Constructs the handler from the {@link Thing}
     *
     * @param thing a non-null {@link Thing} the handler is for
     */
    public RNetZoneHandler(Thing thing) {
        super(thing);

    }

    private void requestZoneInfo() {
        long elapsed = System.currentTimeMillis() - this.lastRefreshRequest;
        if (elapsed > 200) {
            lastRefreshRequest = System.currentTimeMillis();
            scheduler.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    logger.debug("Requesting zone info, id: {}", RNetZoneHandler.this.id);
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.ZONE_INFO, id, (byte) 0));

                    return null;
                }
            });
        } else {
            logger.debug("Did not request zone info as elapsed since last call is: {}", elapsed);
            if (this.lastUpdate != null) {
                processUpdates(this.lastUpdate);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        String id = channelUID.getId();

        switch (id) {
            case RNetConstants.CHANNEL_ZONEBASS:
                if (command instanceof DecimalType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.BASS_SET, this.id,
                            (byte) (((DecimalType) command).intValue() + 10)));
                } else {
                    logger.debug("Received a ZONE BASS channel command with a non DecimalType: {}", command);
                }
                break;
            case RNetConstants.CHANNEL_ZONETREBLE:
                if (command instanceof DecimalType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.TREBLE_SET, this.id,
                            (byte) (((DecimalType) command).intValue() + 10)));
                } else {
                    logger.debug("Received a ZONE TREBLE channel command with a non DecimalType: {}", command);
                }
                break;
            case RioConstants.CHANNEL_ZONEBALANCE:
                if (command instanceof DecimalType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.BALANCE_SET, this.id,
                            (byte) (((DecimalType) command).intValue() + 10)));
                } else {
                    logger.debug("Received a ZONE BALANCE channel command with a non DecimalType: {}", command);
                }
                break;
            case RNetConstants.CHANNEL_ZONETURNONVOLUME:
                if (command instanceof OnOffType) {
                    getSystemHander().sendCommand(
                            RNetProtocolCommands.getCommand(ZoneCommand.TURNONVOLUME_SET, this.id, (byte) (100 / 2)));
                } else if (command instanceof PercentType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.TURNONVOLUME_SET, this.id,
                            (byte) (((PercentType) command).intValue() / 2)));
                } else {
                    logger.debug("Received a ZONE TURN ON VOLUME channel command with a non PercentType/OnOffType: {}",
                            command);
                }
                break;
            case RNetConstants.CHANNEL_ZONELOUDNESS:
                if (command instanceof OnOffType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.LOUDNESS_SET, this.id,
                            command == OnOffType.ON ? (byte) 0x01 : 0));
                } else {
                    logger.debug("Received a ZONE TURN ON VOLUME channel command with a non OnOffType: {}", command);
                }
                break;
            case RNetConstants.CHANNEL_ZONESOURCE:
                if (command instanceof DecimalType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.SOURCE_SET, this.id,
                            (byte) (((DecimalType) command).intValue() - 1)));
                } else {
                    logger.debug("Received a ZONE SOURCE channel command with a non DecimalType: {}", command);
                }
                break;
            case RNetConstants.CHANNEL_ZONESTATUS:
                if (command instanceof OnOffType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.POWER_SET, this.id,
                            command == OnOffType.ON ? (byte) 0x01 : 0));
                } else {
                    logger.debug("Received a ZONE STATUS channel command with a non OnOffType: {}", command);
                }
                break;
            case RNetConstants.CHANNEL_ZONEMUTE:
                if (command instanceof OnOffType) {
                    getSystemHander().sendCommand(
                            RNetProtocolCommands.getCommand(ZoneCommand.MUTE, this.id, RNetProtocolCommands.NO_VALUE));
                    updateState(RNetConstants.CHANNEL_ZONEMUTE, OnOffType.OFF);
                } else {
                    logger.debug("Received a ZONE STATUS channel command with a non OnOffType: {}", command);
                }
                break;
            case RioConstants.CHANNEL_ZONEVOLUME:
                if (command instanceof OnOffType) {
                    getSystemHander().sendCommand(
                            RNetProtocolCommands.getCommand(ZoneCommand.VOLUME_SET, this.id, (byte) (100 / 2)));
                } else if (command instanceof PercentType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.VOLUME_SET, this.id,
                            (byte) (((PercentType) command).intValue() / 2)));
                } else if (command instanceof DecimalType) {
                    getSystemHander().sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.VOLUME_SET, this.id,
                            (byte) (((DecimalType) command).intValue() / 2)));
                } else {
                    logger.debug(
                            "Received a ZONE VOLUME channel command with a non OnOffType/PercentType/DecimalTye: {}",
                            command);
                }
                break;
            default:
                logger.debug("Unknown/Unsupported Channel id: {}", id);
        }

    }

    /**
     * Initializes the bridge. Confirms the configuration is valid and that our parent bridge is a
     * {@link RioControllerHandler}. Once validated, a {@link RioZoneProtocol} is set via
     * {@link #setProtocolHandler(RioZoneProtocol)} and the bridge comes online.
     */
    @Override
    public void initialize() {
        final Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Cannot be initialized without a bridge");
            return;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        final ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No handler specified (null) for the bridge!");
            return;
        }

        final RnetZoneConfig config = getThing().getConfiguration().as(RnetZoneConfig.class);
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            return;
        }

        final int configZone = config.getZone();
        if (configZone < 1 || configZone > 6) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Source must be between 1 and 6: " + configZone);
            return;
        }

        final int configController = config.getController();
        if (configController < 1 || configController > 6) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Controller must be between 1 and 6: " + configZone);
            return;
        }
        this.id = new ZoneId(configController, configZone);
        updateStatus(ThingStatus.ONLINE);
        // requestZoneInfo();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("rnet zone channel linked: {}", channelUID);
        requestZoneInfo();
    }

    public void processUpdates(Collection<ChannelStateUpdate> updates) {

        this.lastUpdate = updates;
        this.lastRefreshRequest = System.currentTimeMillis();
        for (ChannelStateUpdate update : updates) {
            // if we change power, and are doing it from a collection of size one (ie not an overall zone update) then
            // let's reload the other attributes as well and the turn on attributes kick in
            if (updates.size() == 1 && RNetConstants.CHANNEL_ZONESTATUS.equals(update.getChannel())) {
                this.lastRefreshRequest = 0;
                requestZoneInfo();
            }
            updateState(update.getChannel(), update.getState());
        }
    }

    private RNetSystemHandler getSystemHander() {
        return (RNetSystemHandler) this.getBridge().getHandler();
    }

}
