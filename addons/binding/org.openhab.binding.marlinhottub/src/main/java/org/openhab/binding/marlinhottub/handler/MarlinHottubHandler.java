/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.marlinhottub.handler;

import static org.openhab.binding.marlinhottub.MarlinHotTubBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
import org.openhab.binding.marlinhottub.internal.Hottub;
import org.openhab.binding.marlinhottub.internal.Switch;
import org.openhab.binding.marlinhottub.internal.Temperature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * The {@link MarlinHottubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@NonNullByDefault
public class MarlinHottubHandler extends BaseThingHandler {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(MarlinHottubHandler.class);
    @Nullable
    private ScheduledFuture<?> poller;

    private static String restHottub = "/htmobile/rest/hottub/";
    private String hostname = "";
    private XStream xStream;
    private Map<String, UpdateHandler> updateHandlers;

    @SuppressWarnings("null")
    public MarlinHottubHandler(Thing thing) {
        super(thing);
        this.xStream = new XStream(new StaxDriver());
        this.xStream.ignoreUnknownElements();
        this.xStream.setClassLoader(MarlinHottubHandler.class.getClassLoader());
        this.xStream.processAnnotations(new Class[] { Switch.class, Temperature.class, Hottub.class });
        this.updateHandlers = new HashMap<String, UpdateHandler>();
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
                if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (Exception e) {
                // e.printStackTrace();
                logger.error(e.getLocalizedMessage());
                // make thing go offline if isn't reachable
                if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                    String msg = "Unable to reach '" + hostname
                            + ", please check that the 'hostname/ip' setting is correct, or if there is a network problem. Detailed error: '";
                    msg += e.getLocalizedMessage() + "'";
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                }
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
            if (channelUID.getId().equals(SETPOINT)) {
                logger.debug("command {} with channel uid {}, converting to {}/{}", command, channelUID, "setpoint",
                        message);
                this.callRestCommand("setpoint", message);
            }
            if (channelUID.getId().equals(PUMP)) {
                logger.debug("command {} with channel uid {}, converting to {}/{}", command, channelUID, "pump",
                        message);
                this.callRestCommand("pump", message);
            }
            if (channelUID.getId().equals(BLOWER)) {
                logger.debug("command {} with channel uid {}, converting to {}/{}", command, channelUID, "blower",
                        message);
                this.callRestCommand("blower", message);
            }
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception e) {
            // e.printStackTrace();
            logger.error(e.getLocalizedMessage());
            // make thing go offline if isn't reachable
            if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                String msg = "Unable to reach '" + hostname
                        + ", please check that the 'hostname/ip' setting is correct, or if there is a network problem. Detailed error: '";
                msg += e.getLocalizedMessage() + "'";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
            }
        }
    }

    private String getBaseURL() {
        return "http://" + this.hostname + restHottub;
    }

    @SuppressWarnings("null")
    private void callRestCommand(String device, String command) throws IOException {

        String urlParams = "";
        String urlStr = this.getBaseURL() + device + "/" + command;
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
    private String callRestUpdate() throws IOException, InterruptedIOException {

        URL url = new URL(this.getBaseURL());
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

            this.updateHandlers.get(TEMPERATURE).processMessage(hottub.getTemperature().getValue());
            this.updateHandlers.get(SETPOINT).processMessage(hottub.getSetpoint().getValue());
            this.updateHandlers.get(PUMP).processMessage(hottub.getPump().getState());
            this.updateHandlers.get(BLOWER).processMessage(hottub.getBlower().getState());
            this.updateHandlers.get(HEATER).processMessage(hottub.getHeater().getState());
        }
    }

    @SuppressWarnings("null")
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

        Channel channel;

        // channel = this.getThing().getChannel(TEMPERATURE);
        // assert channel != null;
        // this.updateHandlers.put(TEMPERATURE, new UpdateHandler(this, channel, DecimalType.class));
        //
        // channel = this.getThing().getChannel(SETPOINT);
        // assert channel != null;
        // this.updateHandlers.put(SETPOINT, new UpdateHandler(this, channel, DecimalType.class));
        //
        // channel = this.getThing().getChannel(PUMP);
        // assert channel != null;
        // this.updateHandlers.put(PUMP, new UpdateHandler(this, channel, OnOffType.class));
        //
        // channel = this.getThing().getChannel(BLOWER);
        // assert channel != null;
        // this.updateHandlers.put(BLOWER, new UpdateHandler(this, channel, OnOffType.class));
        //
        // channel = this.getThing().getChannel(HEATER);
        // assert channel != null;
        // this.updateHandlers.put(HEATER, new UpdateHandler(this, channel, OnOffType.class));
        this.installChannelHandler("temperature", "temperature", "Number", DecimalType.class);
        this.installChannelHandler("setpoint", "setpoint", "Number", DecimalType.class);
        this.installChannelHandler("pump", "pump", "Switch", OnOffType.class);
        this.installChannelHandler("blower", "blower", "Switch", OnOffType.class);
        this.installChannelHandler("heater", "heater", "Switch", OnOffType.class);

        updateStatus(ThingStatus.ONLINE);

        // create a poller task that polls the REST interface
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    String restResponse = MarlinHottubHandler.this.callRestUpdate();
                    // in case we come back from an outage -> set status online
                    if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    MarlinHottubHandler.this.parseAndUpdate(restResponse);
                } catch (InterruptedIOException ie) {
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                    // make thing go offline if the hottub isn't reachable
                    if (!getThing().getStatus().equals(ThingStatus.OFFLINE)) {
                        String msg = "Unable to reach '" + hostname
                                + "', please check that the 'hostname/ip' setting is correct, or if there is a network problem. Detailed error: '";
                        msg += e.getMessage() + "'";
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                    }
                }
            }
        };

        this.poller = this.scheduler.scheduleWithFixedDelay(task, 1, 15, TimeUnit.SECONDS);
    }

    private void installChannelHandler(String channelID, String label, String allowedTypes,
            Class<? extends State> varType) {
        Channel ch = this.getThing().getChannel(channelID);
        if (ch == null) {
            throw new IllegalStateException("channel for " + channelID + " not present");
        } else {
            this.updateHandlers.put(channelID, new UpdateHandler(this, ch, varType));
        }
    }

    @Override
    public void dispose() {
        if (this.poller != null) {
            this.poller.cancel(true);
        }
        super.dispose();
    }

    class UpdateHandler {
        private MarlinHottubHandler handler;
        private Channel channel;
        private String currentState = "";
        private final ArrayList<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();

        UpdateHandler(MarlinHottubHandler handler, Channel channel, Class<? extends State> acceptedType) {
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
