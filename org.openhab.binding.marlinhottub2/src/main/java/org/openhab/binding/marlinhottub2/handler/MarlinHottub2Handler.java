/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.marlinhottub2.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.binding.marlinhottub2.MarlinHottub2BindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * The {@link MarlinHotTubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@NonNullByDefault
public class MarlinHottub2Handler extends BaseThingHandler {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(MarlinHottub2Handler.class);
    // @SuppressWarnings("nonnull")
    // private ScheduledFuture<?> poller = null;

    private static String restHottub = "/htmobile/rest/hottub/";
    private String hostname = "";
    private XStream xStream;
    private Map<String, UpdateHandler> updateHandlers;
    private ScheduledFuture<?> poller;

    @SuppressWarnings("null")
    public MarlinHottub2Handler(Thing thing) {
        super(thing);
        // this.poller = this.scheduler.scheduleWithFixedDelay(ta, 1, 15, TimeUnit.SECONDS);
        // this.poller = this.scheduler.scheduleWithFixedDelay(ta, 1, 15, TimeUnit.SECONDS);
        this.xStream = new XStream(new StaxDriver());
        this.xStream.ignoreUnknownElements();
        this.xStream.setClassLoader(MarlinHottub2Handler.class.getClassLoader());
        this.xStream.processAnnotations(new Class[] { Switch.class, Temperature.class, Hottub.class });
        this.updateHandlers = new HashMap<String, UpdateHandler>();
        this.poller = null;
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");

        if (command instanceof RefreshType) {

            try {
                String restResponse = this.callRestUpdate();
                this.parseAndUpdate(restResponse);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        Object state = command.toString();

        if (command instanceof DecimalType) {
            state = ((DecimalType) command).toBigDecimal().toBigInteger();
        } else if (command instanceof OnOffType) {
            state = command.equals(OnOffType.ON) ? "ON" : "OFF";
            // } else if (command instanceof OpenClosedType) {
            // state = command.equals(OpenClosedType.OPEN) ? "ON" : "OFF";
        } else {
            logger.debug("command {} with channel uid {} not understood", command, channelUID);
        }

        String message = state.toString();

        try {
            // only if not refresh, handle below
            if (channelUID.getId().equals(MarlinHottub2BindingConstants.SETPOINT)) {
                logger.debug("command {} with channel uid {}, converting to {}/{}", command, channelUID, "setpoint",
                        message);
                this.callRestCommand("setpoint", message);
            }
            if (channelUID.getId().equals(MarlinHottub2BindingConstants.PUMP)) {
                logger.debug("command {} with channel uid {}, converting to {}/{}", command, channelUID, "pump",
                        message);
                this.callRestCommand("pump", message);
            }
            if (channelUID.getId().equals(MarlinHottub2BindingConstants.BLOWER)) {
                logger.debug("command {} with channel uid {}, converting to {}/{}", command, channelUID, "blower",
                        message);
                this.callRestCommand("blower", message);
            }
        } catch (Exception e) {
            // TODO Figure what exceptions we get, and how to handle them
            e.printStackTrace();
        }
    }

    private String getURLString() {
        return "http://" + this.hostname + restHottub;
    }

    @SuppressWarnings("null")
    private void callRestCommand(String device, String command) throws IOException {

        String urlParams = "";
        String urlStr = this.getURLString() + device + "/" + command;
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");

        try {
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(urlParams);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String result = "";
            while ((line = reader.readLine()) != null) {
                result += line;
            }
            if (result == null || result.indexOf("<ok succeeded=\"true\"><status>200</status></ok>") == -1) {
                throw new IOException(result);
            }
        } finally {
            IOUtils.closeQuietly(connection.getInputStream());
        }
    }

    @SuppressWarnings("null")
    private String callRestUpdate() throws IOException {

        URL url = new URL(this.getURLString());
        URLConnection connection = url.openConnection();
        try {
            String response = IOUtils.toString(connection.getInputStream());
            logger.trace("hottub response = {}", response);
            return response;
        } finally {
            IOUtils.closeQuietly(connection.getInputStream());
        }
    }

    @SuppressWarnings("null")
    private void parseAndUpdate(String rest) {
        Object msg = xStream.fromXML(rest);
        if (msg instanceof Hottub) {
            Hottub hottub = (Hottub) msg;

            this.updateHandlers.get(MarlinHottub2BindingConstants.TEMPERATURE)
                    .processMessage(hottub.getTemperature().getValue());

            this.updateHandlers.get(MarlinHottub2BindingConstants.SETPOINT)
                    .processMessage(hottub.getSetpoint().getValue());

            this.updateHandlers.get(MarlinHottub2BindingConstants.PUMP).processMessage(hottub.getPump().getState());

            this.updateHandlers.get(MarlinHottub2BindingConstants.BLOWER).processMessage(hottub.getBlower().getState());

            this.updateHandlers.get(MarlinHottub2BindingConstants.HEATER).processMessage(hottub.getHeater().getState());

        }
    }

    @Override
    public void initialize() {

        this.hostname = (String) getThing().getConfiguration().get("hostname");

        this.installChannelHandler(MarlinHottub2BindingConstants.TEMPERATURE, MarlinHottub2BindingConstants.TEMPERATURE,
                "Number", DecimalType.class);

        this.installChannelHandler(MarlinHottub2BindingConstants.SETPOINT, MarlinHottub2BindingConstants.SETPOINT,
                "Number", DecimalType.class);

        this.installChannelHandler(MarlinHottub2BindingConstants.PUMP, MarlinHottub2BindingConstants.PUMP, "Switch",
                OnOffType.class);

        this.installChannelHandler(MarlinHottub2BindingConstants.BLOWER, MarlinHottub2BindingConstants.BLOWER, "Switch",
                OnOffType.class);

        this.installChannelHandler(MarlinHottub2BindingConstants.HEATER, MarlinHottub2BindingConstants.HEATER, "Switch",
                OnOffType.class);

        // this.updateHandlers.put(MarlinHottub2BindingConstants.SETPOINT, new UpdateHandler(this,
        // this.getThing().getChannel(MarlinHottub2BindingConstants.SETPOINT), DecimalType.class));
        // this.updateHandlers.put(MarlinHottub2BindingConstants.PUMP, new UpdateHandler(this,
        // this.getThing().getChannel(MarlinHottub2BindingConstants.PUMP), OnOffType.class));
        // this.updateHandlers.put(MarlinHottub2BindingConstants.BLOWER, new UpdateHandler(this,
        // this.getThing().getChannel(MarlinHottub2BindingConstants.BLOWER), OnOffType.class));
        // this.updateHandlers.put(MarlinHottub2BindingConstants.HEATER, new UpdateHandler(this,
        // this.getThing().getChannel(MarlinHottub2BindingConstants.HEATER), OnOffType.class));

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // create a poller task that polls the REST interface
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    String restResponse = MarlinHottub2Handler.this.callRestUpdate();
                    MarlinHottub2Handler.this.parseAndUpdate(restResponse);
                } catch (Exception e) {
                    // TODO figure out what exceptions we get, and what to do about them
                    e.printStackTrace();
                }
            }
        };

        this.poller = this.scheduler.scheduleWithFixedDelay(task, 1, 15, TimeUnit.SECONDS);
    }

    private void installChannelHandler(String channelID, String label, String allowedTypes,
            Class<? extends State> varType) {
        Channel ch;
        ch = this.getThing().getChannel(channelID);
        if (ch == null) {
            throw new IllegalStateException("channel for " + channelID + " not present");
        }
        this.updateHandlers.put(channelID, new UpdateHandler(this, ch, varType));
    }

    @SuppressWarnings("null")
    @Override
    public void dispose() {
        if (this.poller != null) {
            this.poller.cancel(true);
        }
        super.dispose();
    }

    class UpdateHandler {
        private MarlinHottub2Handler handler;
        private Channel channel;
        private String currentState = "";
        private final ArrayList<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();

        UpdateHandler(MarlinHottub2Handler handler, Channel channel, Class<? extends State> acceptedType) {
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