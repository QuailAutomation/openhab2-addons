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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.eagle200.Eagle200BindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Eagle200MeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@NonNullByDefault
public class Eagle200MeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(Eagle200MeterHandler.class);
    private Runnable scraper;
    private Map<String, String> lastupdates = new HashMap<String, String>();

    public Eagle200MeterHandler(Thing thing) {
        super(thing);
        this.scraper = new Runnable() {

            @SuppressWarnings("null")
            @Override
            public void run() {
                Configuration config = Eagle200MeterHandler.this.getConfig();
                String addr = (String) config.get(Eagle200BindingConstants.THING_CONFIG_HWADDRESS);
                try {
                    Bridge bridge = getBridge();
                    if (bridge != null && bridge.getHandler() != null) {
                        Map<String, String> update = ((Eagle200BridgeHandler) bridge.getHandler()).getConnection()
                                .queryMeter(addr);
                        if (!Eagle200MeterHandler.this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                            Eagle200MeterHandler.this.updateStatus(ThingStatus.ONLINE);
                        }
                        Eagle200MeterHandler.this.updateChannels(update);
                    }
                } catch (IOException e) {
                    logger.warn("connection to eagle caused IO error", e);
                    Eagle200MeterHandler.this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    return;
                }
            }
        };
    }

    @Override
    public void initialize() {
        this.initChannels();
        Configuration config = Eagle200MeterHandler.this.getConfig();
        BigDecimal freq = (BigDecimal) config.get(Eagle200BindingConstants.THING_CONFIG_SCANFREQUENCY);
        if (freq == null) {
            freq = new BigDecimal(60);
        }
        this.scheduler.scheduleWithFixedDelay(this.scraper, 1, freq.intValue(), TimeUnit.SECONDS);
    }

    @SuppressWarnings("null")
    private void initChannels() {
        Bridge bridge = getBridge();
        Configuration config = Eagle200MeterHandler.this.getConfig();
        String addr = (String) config.get(Eagle200BindingConstants.THING_CONFIG_HWADDRESS);

        if (bridge == null || bridge.getHandler() == null) {
            logger.debug("eagle200 has no bridge yet");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        Map<String, String> update;
        try {
            update = ((Eagle200BridgeHandler) bridge.getHandler()).getConnection().queryMeter(addr);
        } catch (Exception e) {
            logger.warn("connection to eagle200 caused IO error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        ThingBuilder builder = this.editThing();
        SortedMap<String, String> sorted = new TreeMap<String, String>(update);
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (this.getThing().getChannel(this.getChannelName(entry.getKey())) == null) {
                ChannelUID uid = this.getChannelUID(entry.getKey());
                Channel channel = ChannelBuilder.create(uid, "String").withLabel(entry.getKey().replace("zigbee:", ""))
                        .build();
                builder.withChannel(channel);
            }
        }
        this.updateThing(builder.build());

        updateStatus(ThingStatus.ONLINE);
    }

    private ChannelUID getChannelUID(String key) {
        return new ChannelUID(this.getThing().getUID(), this.getChannelName(key));
    }

    private String getChannelName(String key) {
        return key.replace("zigbee:", "");
    }

    @SuppressWarnings({ "null", "unused" })
    private void updateChannels(Map<String, String> updates) {

        for (Map.Entry<String, String> update : updates.entrySet()) {
            String lastvalue = this.lastupdates.get(update.getKey());
            if (lastvalue == null) {
                this.updateState(this.getChannelUID(update.getKey()), new StringType(update.getValue()));
            } else if (update.getValue() != null && !update.getValue().equals(this.lastupdates.get(update.getKey()))) {
                this.updateState(this.getChannelUID(update.getKey()), new StringType(update.getValue()));
            }
        }
        this.lastupdates = updates;
    }

    @Override
    public void handleConfigurationUpdate(@NonNull Map<@NonNull String, @NonNull Object> configurationParameters) {
        logger.debug("handleConfigurationUpdate for " + this.getThing().getUID());
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void bridgeStatusChanged(@NonNull ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged for " + this.getThing().getUID());
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands, async updates only
    }
}
