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
package org.openhab.binding.marlinfireplace.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
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
import org.openhab.binding.marlinfireplace.MarlinFireplaceBindingConstants;
import org.openhab.core.events.EventPublisher;
import org.openhab.io.transport.mqtt.MqttMessageConsumer;
import org.openhab.io.transport.mqtt.MqttMessageProducer;
import org.openhab.io.transport.mqtt.MqttSenderChannel;
import org.openhab.io.transport.mqtt.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MarlinFireplaceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Hentschel - Initial contribution
 */
public class MarlinFireplaceHandler extends BaseThingHandler {

    public enum MessageType {
        COMMAND,
        STATE
    }

    @SuppressWarnings({ "null", "unused" })
    private final Logger logger = LoggerFactory.getLogger(MarlinFireplaceHandler.class);
    private MqttService mqttService;
    private Map<String, FireplaceMQTTConsumer> stateUpdateHandlers;
    private Map<String, FirePlaceMQTTProducer> commandHandlers;
    private String brokerName;

    public MarlinFireplaceHandler(Thing thing) {
        super(thing);
        this.brokerName = null;
        this.commandHandlers = new HashMap<String, FirePlaceMQTTProducer>();
        this.stateUpdateHandlers = new HashMap<String, FireplaceMQTTConsumer>();
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {

        this.brokerName = (String) getThing().getConfiguration().get("mqttbroker");
        String pubTopicPart = (String) getThing().getConfiguration().get("basetopic"); // 'out' topic
        String subTopicPart = (String) getThing().getConfiguration().get("basetopic"); // 'in' topic
        if (pubTopicPart.endsWith("/")) {
            pubTopicPart = pubTopicPart.substring(0, pubTopicPart.length() - 1);
        }
        if (subTopicPart.endsWith("/")) {
            subTopicPart = subTopicPart.substring(0, subTopicPart.length() - 1);
        }
        pubTopicPart = pubTopicPart + "/in/web";
        subTopicPart = subTopicPart + "/out/web";

        if (this.mqttService == null) {
            this.logger.error("No MQTT service yet?");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device, no MQTT broker service available");
            return;
        }

        Channel channel = null;
        String subtopic = null;
        String pubtopic = null;

        channel = this.getThing().getChannel(MarlinFireplaceBindingConstants.TEMPERATURE);
        subtopic = subTopicPart + "/temp";
        this.stateUpdateHandlers.put(MarlinFireplaceBindingConstants.TEMPERATURE,
                new FireplaceMQTTConsumer(subtopic, this, channel, DecimalType.class));
        this.mqttService.registerMessageConsumer(brokerName,
                this.stateUpdateHandlers.get(MarlinFireplaceBindingConstants.TEMPERATURE));

        channel = this.getThing().getChannel(MarlinFireplaceBindingConstants.SETPOINT);
        subtopic = subTopicPart + "/set";
        pubtopic = pubTopicPart + "/set";
        this.stateUpdateHandlers.put(MarlinFireplaceBindingConstants.SETPOINT,
                new FireplaceMQTTConsumer(subtopic, this, channel, DecimalType.class));
        this.mqttService.registerMessageConsumer(brokerName,
                this.stateUpdateHandlers.get(MarlinFireplaceBindingConstants.SETPOINT));
        this.commandHandlers.put(MarlinFireplaceBindingConstants.SETPOINT,
                new FirePlaceMQTTProducer(pubtopic, channel));
        this.mqttService.registerMessageProducer(brokerName,
                this.commandHandlers.get(MarlinFireplaceBindingConstants.SETPOINT));

        channel = this.getThing().getChannel(MarlinFireplaceBindingConstants.MODE);
        subtopic = subTopicPart + "/mode";
        pubtopic = pubTopicPart + "/mode";
        this.stateUpdateHandlers.put(MarlinFireplaceBindingConstants.MODE,
                new FireplaceMQTTConsumer(subtopic, this, channel, StringType.class));
        this.mqttService.registerMessageConsumer(brokerName,
                this.stateUpdateHandlers.get(MarlinFireplaceBindingConstants.MODE));
        this.commandHandlers.put(MarlinFireplaceBindingConstants.MODE, new FirePlaceMQTTProducer(pubtopic, channel));
        this.mqttService.registerMessageProducer(brokerName,
                this.commandHandlers.get(MarlinFireplaceBindingConstants.MODE));

        channel = this.getThing().getChannel(MarlinFireplaceBindingConstants.STATE);
        subtopic = subTopicPart + "/state";
        this.stateUpdateHandlers.put(MarlinFireplaceBindingConstants.STATE,
                new FireplaceMQTTConsumer(subtopic, this, channel, OnOffType.class));
        this.mqttService.registerMessageConsumer(brokerName,
                this.stateUpdateHandlers.get(MarlinFireplaceBindingConstants.STATE));

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        for (FireplaceMQTTConsumer stateHandler : this.stateUpdateHandlers.values()) {
            this.mqttService.unregisterMessageConsumer(this.brokerName, stateHandler);
        }
        this.stateUpdateHandlers.clear();
        for (FirePlaceMQTTProducer commandHandler : this.commandHandlers.values()) {
            this.mqttService.unregisterMessageProducer(this.brokerName, commandHandler);
        }
        this.commandHandlers.clear();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        FirePlaceMQTTProducer handler = this.commandHandlers.get(channelUID.getId());
        if (handler == null) {
            this.logger.debug("no command handler found for {}", channelUID.getId());
            return;
        }
        try {
            handler.handleCommand(channelUID, command);
        } catch (Exception e) {
            this.logger.error("error handling {}, {}", channelUID.getId(), e.getMessage());
        }
    }

    @Override
    /**
     * overide to make accessible to MQTT helpers
     */
    public void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }

    /**
     * Set MQTT Service from DS.
     *
     * @param mqttService
     *            to set.
     */
    public void setMqttService(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    /**
     * Unset MQTT Service from DS.
     *
     * @param mqttService
     *            to remove.
     */
    public void unsetMqttService(MqttService mqttService) {
        this.mqttService = null;
    }

}

class FireplaceMQTTConsumer implements MqttMessageConsumer {

    EventPublisher eventPublisher; // <-- should not be needed anymore??
    private MarlinFireplaceHandler handler;
    private String topic;
    private Channel channel;
    private final ArrayList<Class<? extends State>> acceptedDataTypes = new ArrayList<Class<? extends State>>();

    FireplaceMQTTConsumer(String topic, MarlinFireplaceHandler handler, Channel channel,
            Class<? extends State> acceptedType) {
        super();
        this.setTopic(topic);
        this.handler = handler;
        this.channel = channel;
        acceptedDataTypes.add(acceptedType);
    }

    @Override
    public void processMessage(String topic, byte[] message) {
        String value = new String(message).toUpperCase();

        State state = this.getState(value, this.acceptedDataTypes);

        this.handler.updateState(this.channel.getUID(), state);
    }

    /**
     * Convert a string representation of a state to an openHAB State.
     *
     * @param value
     *            string representation of State
     * @param acceptedDataTypes
     *            list of accepted data types for converting value
     * @return State
     */
    protected State getState(String value, List<Class<? extends State>> acceptedDataTypes) {

        return TypeParser.parseState(acceptedDataTypes, value);
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}

class FirePlaceMQTTProducer implements MqttMessageProducer {

    private String topic;
    private Channel channel;
    private MqttSenderChannel mqttChannel = null;

    public FirePlaceMQTTProducer(String topic, Channel channel) {
        this.topic = topic;
        this.channel = channel;
    }

    @Override
    public void setSenderChannel(MqttSenderChannel channel) {
        this.mqttChannel = channel;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void handleCommand(ChannelUID channelUID, Command command) throws Exception {

        if (command instanceof RefreshType) {
            // how to handle refresh?
            return;
        }
        Object state = command.toString();

        if (command instanceof DecimalType) {
            state = ((DecimalType) command).toBigDecimal();
        } else if (command instanceof OnOffType) {
            state = command.equals(OnOffType.ON) ? "1" : "0";
        } else if (command instanceof OpenClosedType) {
            state = command.equals(OpenClosedType.OPEN) ? "1" : "0";
        }

        String message = state.toString();

        this.mqttChannel.publish(this.topic, message.getBytes());
    }
}