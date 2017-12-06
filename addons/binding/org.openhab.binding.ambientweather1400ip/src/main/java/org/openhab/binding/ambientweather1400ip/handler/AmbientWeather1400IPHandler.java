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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmbientWeather1400IPHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hentschel - Initial contribution
 */
public class AmbientWeather1400IPHandler extends BaseThingHandler {

    private static String livedata = "/livedata.htm";

    private final Logger logger = LoggerFactory.getLogger(AmbientWeather1400IPHandler.class);
    private String hostname = "";
    private Map<String, UpdateHandler> updateHandlers;
    private Map<String, String> inputMapper;
    private ScheduledFuture<?> poller;

    public AmbientWeather1400IPHandler(Thing thing) {
        super(thing);
        this.updateHandlers = new HashMap<String, UpdateHandler>();
        this.inputMapper = new HashMap<String, String>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO: handle refresh? We're polling anyway, and the 1st update comes after 1 second...
    }

    @Override
    public void initialize() {

        if (this.poller != null) {
            logger.debug("leftover poller task still running, attempting to cancel");
            this.poller.cancel(true);
        }

        this.hostname = (String) getThing().getConfiguration().get("hostname");
        // basic sanity
        if (this.hostname == null || this.hostname.equals("")) {
            String msg = "Invalid hostname '" + this.hostname + ", please check configuration";
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

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

        // updateStatus(ThingStatus.ONLINE);

        // create a poller task that polls the web page
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    String webResponse = AmbientWeather1400IPHandler.this.callWebUpdate();

                    // in case we come back from an outage -> set status online
                    if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    AmbientWeather1400IPHandler.this.parseAndUpdate(webResponse);
                } catch (Exception e) {
                    // e.printStackTrace();
                    logger.error(e.getLocalizedMessage());
                    // make thing go offline if the weather station isn't reachable
                    if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                        String msg = "Unable to reach '" + hostname
                                + "', please check that the 'hostname/ip' setting is correct, or if there is a network problem. Detailed error: '";
                        msg += e.getLocalizedMessage() + "'";
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                    }
                }
            }
        };

        this.poller = this.scheduler.scheduleWithFixedDelay(task, 1, 30, TimeUnit.SECONDS);
    }

    @SuppressWarnings("null")
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

    @Override
    public void dispose() {
        if (this.poller != null) {
            this.poller.cancel(true);
        }
        super.dispose();
    }

    private void createChannel(String chanName, Class<? extends State> type, String htmlName) {
        Channel channel = this.getThing().getChannel(chanName);
        assert channel != null;
        this.updateHandlers.put(chanName, new UpdateHandler(this, channel, type));
        this.inputMapper.put(htmlName, chanName);
    }

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

}
