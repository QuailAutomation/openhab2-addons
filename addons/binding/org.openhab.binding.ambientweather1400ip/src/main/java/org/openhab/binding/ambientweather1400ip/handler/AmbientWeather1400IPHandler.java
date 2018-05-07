/**
 * Copyright (c) 2014,2017 by the respective copyright holders.
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
package org.openhab.binding.ambientweather1400ip.handler;

import static org.openhab.binding.ambientweather1400ip.AmbientWeather1400IPBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.binding.ambientweather1400ip.AmbientWeather1400IPBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmbientWeather1400IPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hentschel - Initial contribution
 */
public class AmbientWeather1400IPHandler extends BaseThingHandler {

    class UpdateHandler {
        private AmbientWeather1400IPHandler handler;
        private Channel channel;
        private String currentState = "";
        private final ArrayList<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();

        UpdateHandler(AmbientWeather1400IPHandler handler, Channel channel, Class<? extends State> acceptedType) {
            super();
            this.handler = handler;
            this.channel = channel;
            acceptedDataTypes.add(acceptedType);
        }

        @SuppressWarnings("null")
        public void processMessage(String message) {
            String value = message.toUpperCase();
            // only if there was a real change
            if (value.equalsIgnoreCase(this.currentState) == false) {
                this.currentState = value;
                State state = TypeParser.parseState(this.acceptedDataTypes, value);
                this.handler.updateState(this.channel.getUID(), state);
            }
        }
    }

    private static String livedata = "/livedata.htm";
    private final Logger logger = LoggerFactory.getLogger(AmbientWeather1400IPHandler.class);
    private String hostname = "";
    private int scanrate = 60;
    private Map<String, UpdateHandler> updateHandlers;
    private Map<String, String> inputMapper;

    private ScheduledFuture<?> poller = null;
    private Runnable updatetask = null;

    public AmbientWeather1400IPHandler(Thing thing) {
        super(thing);
        this.updateHandlers = new HashMap<String, UpdateHandler>();
        this.inputMapper = new HashMap<String, String>();

        this.updatetask = new Runnable() {
            @Override
            public void run() {
                try {
                    long start = System.currentTimeMillis();
                    String webResponse = AmbientWeather1400IPHandler.this.callWebUpdate();
                    logger.trace("AmbientWeather1400 gateway call took {} msec", (System.currentTimeMillis() - start));
                    // in case we come back from an outage -> set status online
                    if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    AmbientWeather1400IPHandler.this.parseAndUpdate(webResponse);
                } catch (Throwable e) {
                    logger.error(e.getMessage());
                    if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                        String msg = "Unable to reach '" + hostname
                                + "', please check that the 'hostname/ip' setting is correct, or if there is a network problem. Detailed error: '";
                        msg += e.getMessage() + "'";
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                    }
                }
            }
        };
    }

    @Override
    public void initialize() {

        if (this.poller != null && !this.poller.isDone()) {
            logger.debug("leftover poller task still running, attempting to cancel");
            this.poller.cancel(true);
        }

        this.hostname = (String) getThing().getConfiguration()
                .get(AmbientWeather1400IPBindingConstants.CONFIG_HOSTNAME);
        // basic sanity
        if (this.hostname == null || this.hostname.equals("")) {
            String msg = "Invalid hostname '" + this.hostname + ", please check configuration";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        BigDecimal freq = (BigDecimal) getThing().getConfiguration()
                .get(AmbientWeather1400IPBindingConstants.CONFIG_SCANRATE);

        if (freq == null) {
            freq = new BigDecimal(60);
        }
        this.setScanrate(freq.intValue());

        this.createChannel(INDOOR_TEMP, DecimalType.class, "inTemp");
        this.createChannel(OUTDOOR_TEMP, DecimalType.class, "outTemp");
        this.createChannel(INDOOR_HUMIDITY, DecimalType.class, "inHumi");
        this.createChannel(OUTDOOR_HUMIDITY, DecimalType.class, "outHumi");
        this.createChannel(ABS_PRESSURE, DecimalType.class, "AbsPress");
        this.createChannel(REL_PRESSURE, DecimalType.class, "RelPress");
        this.createChannel(WIND_DIRECTION, DecimalType.class, "windir");
        this.createChannel(WIND_SPEED, DecimalType.class, "avgwind");
        this.createChannel(WIND_GUST, DecimalType.class, "gustspeed");
        this.createChannel(SOLAR_RADIATION, DecimalType.class, "solarrad");
        this.createChannel(UV, DecimalType.class, "uv");
        this.createChannel(UVI, DecimalType.class, "uvi");
        this.createChannel(HOURLY_RAIN, DecimalType.class, "rainofhourly");
        this.createChannel(DAILY_RAIN, DecimalType.class, "rainofdaily");
        this.createChannel(WEEKLY_RAIN, DecimalType.class, "rainofweekly");
        this.createChannel(MONTHLY_RAIN, DecimalType.class, "rainofmonthly");
        this.createChannel(YEARLY_RAIN, DecimalType.class, "rainofyearly");
        this.createChannel(BATTERY_OUT, StringType.class, "outBattSta1");
        this.createChannel(BATTERY_IN, StringType.class, "inBattSta");

        // stay offline, the poller will figure the right state in a sec...
        this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                "Contacting weather station...");

        this.poller = this.scheduler.scheduleWithFixedDelay(this.updatetask, 1, this.getScanrate(), TimeUnit.SECONDS);
    }

    @SuppressWarnings("null")
    @Override
    public void handleConfigurationUpdate(@NonNull Map<@NonNull String, @NonNull Object> config) {

        boolean changed = false;

        String hostname_new = (String) config.get(AmbientWeather1400IPBindingConstants.CONFIG_HOSTNAME);
        if (hostname_new != null && !hostname_new.equals(this.hostname)) {
            this.hostname = hostname_new;
            changed = true;
        }

        BigDecimal scanrate_new = (BigDecimal) config.get(AmbientWeather1400IPBindingConstants.CONFIG_SCANRATE);
        if (scanrate_new != null && this.getScanrate() != scanrate_new.intValue()) {
            this.setScanrate(scanrate_new.intValue());
            changed = true;
        }

        if (changed) {
            if (this.poller != null && !this.poller.isDone()) {
                this.poller.cancel(true);
            }
            this.poller = this.scheduler.scheduleWithFixedDelay(this.updatetask, 1, this.getScanrate(),
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        if (this.poller != null && !this.poller.isDone()) {
            this.poller.cancel(true);
        }
        this.inputMapper.clear();
        this.updateHandlers.clear();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands but refresh to handle for this device
        if (command instanceof RefreshType) {
            // only allow one refresh channel poll
            // every update updates all channels anyways, so no need to blast the weather station with the
            // same request just run the web get + parse task inline, it will do the right thing
            if (channelUID.getId().equals(OUTDOOR_TEMP)) {
                if (this.updatetask != null) {
                    this.updatetask.run();
                }
            }
        }
    }

    private void createChannel(String chanName, Class<? extends State> type, String htmlName) {
        Channel channel = this.getThing().getChannel(chanName);
        assert channel != null;
        this.updateHandlers.put(chanName, new UpdateHandler(this, channel, type));
        this.inputMapper.put(htmlName, chanName);
    }

    private String callWebUpdate() throws IOException {

        String urlStr = "http://" + this.hostname + livedata;
        URL url = new URL(urlStr);
        URLConnection connection = url.openConnection();
        try {
            String response = IOUtils.toString(connection.getInputStream());
            logger.trace("AWS response = {}", response);
            return response;
        } finally {
            IOUtils.closeQuietly(connection.getInputStream());
        }
    }

    private long getScanrate() {
        return this.scanrate;
    }

    private void setScanrate(int value) {
        this.scanrate = value;
    }

    @SuppressWarnings("null")
    private void parseAndUpdate(String html) {

        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("input");
        logger.trace("found {} inputs", elements.size());
        for (Element element : elements) {
            String elementName = element.attr("name");
            logger.trace("found input element with name {} ", elementName);
            String channelName = this.inputMapper.get(elementName);
            if (channelName != null) {
                logger.trace("found channel name {} for element {} ", channelName, elementName);
                String value = element.attr("value");
                logger.trace("found channel name {} for element {}, value is {} ", channelName, elementName, value);
                this.updateHandlers.get(channelName).processMessage(value);
            } else {
                logger.trace("no channel found for input element {} ", elementName);
            }
        }
    }

}
