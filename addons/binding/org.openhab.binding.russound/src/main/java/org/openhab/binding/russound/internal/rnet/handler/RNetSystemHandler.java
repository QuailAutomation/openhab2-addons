/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.rnet.BusParser;
import org.openhab.binding.russound.internal.rnet.PowerChangeParser;
import org.openhab.binding.russound.internal.rnet.RNetConstants;
import org.openhab.binding.russound.internal.rnet.RNetProtocolCommands;
import org.openhab.binding.russound.internal.rnet.RNetSystemConfig;
import org.openhab.binding.russound.internal.rnet.SourceChangeParser;
import org.openhab.binding.russound.internal.rnet.VolumeChangeParser;
import org.openhab.binding.russound.internal.rnet.ZoneId;
import org.openhab.binding.russound.internal.rnet.ZoneInfoParser;
import org.openhab.binding.russound.internal.rnet.ZoneStateUpdate;
import org.openhab.binding.russound.internal.rnet.RNetProtocolCommands.ZoneCommand;
import org.openhab.binding.russound.internal.rnet.connection.ConnectionProvider;
import org.openhab.binding.russound.internal.rnet.connection.ConnectionStateListener;
import org.openhab.binding.russound.internal.rnet.connection.DeviceConnection;
import org.openhab.binding.russound.internal.rnet.connection.InputHander;
import org.openhab.binding.russound.internal.rnet.connection.NoConnectionException;
import org.openhab.binding.russound.internal.rnet.connection.RNetInputStreamParser;
import org.openhab.binding.russound.internal.rnet.connection.SerialConnectionProvider;
import org.openhab.binding.russound.internal.rnet.connection.TcpConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The bridge handler for a Russound System. This is the entry point into the whole russound system and is generally
 * points to the main controller. This implementation must be attached to a {@link RNetSystemHandler} bridge.
 *
 * @author Craig Hamilton
 */
public class RNetSystemHandler extends BaseBridgeHandler {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(RNetSystemHandler.class);

    /**
     * The configuration for the system - will be recreated when the configuration changes and will be null when not
     * online
     */
    // private RNetSystemConfig config;
    /**
     * These bus parser are responsible for examining a message and letting us know if they denote a BusAction
     */
    private Set<BusParser> busParsers = new HashSet<BusParser>();
    /**
     * The lock used to control access to {@link #config}
     */
    private final ReentrantLock configLock = new ReentrantLock();

    /**
     * The {@link SocketSession} session to the switch. Will be null if not connected.
     */
    private DeviceConnection session;

    /**
     * The lock used to control access to {@link #session}
     */
    private final ReentrantLock sessionLock = new ReentrantLock();

    /**
     * The retry connection event - will only be created when we are retrying the connection attempt
     */
    private ScheduledFuture<?> retryConnection;

    /**
     * The lock used to control access to {@link #retryConnection}
     */
    private final ReentrantLock retryConnectionLock = new ReentrantLock();

    /**
     * The ping event - will be non-null when online (null otherwise)
     */
    private ScheduledFuture<?> ping;

    /**
     * The lock used to control access to {@link #ping}
     */
    private final ReentrantLock pingLock = new ReentrantLock();

    /**
     * Constructs the handler from the {@link Bridge}
     *
     * @param bridge a non-null {@link Bridge} the handler is for
     */
    public RNetSystemHandler(Bridge bridge) {
        super(bridge);
        busParsers.add(new VolumeChangeParser());
        busParsers.add(new PowerChangeParser());
        busParsers.add(new SourceChangeParser());
        busParsers.add(new ZoneInfoParser());

    }

    @Override
    public void dispose() {
        super.dispose();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Disconnecting from Russound System");
        disconnect();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            return;
        }

        // for all on/off zone is irrelevant, however to keep api same will just pass in a 'dummy' ref
        ZoneId dummyZone = new ZoneId(0, 0);
        switch (channelUID.getId()) {
            case RNetConstants.CHANNEL_SYSALLON:
                if (command instanceof OnOffType && OnOffType.ON.equals(command)) {
                    sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.ALLONOFF_SET, dummyZone, (byte) 0x01));
                } else {
                    logger.debug("Received a ZONE STATUS channel command with a non OnOffType: {}", command);
                }
                break;
            case RNetConstants.CHANNEL_SYSALLOFF:
                if (command instanceof OnOffType && OnOffType.ON.equals(command)) {
                    sendCommand(RNetProtocolCommands.getCommand(ZoneCommand.ALLONOFF_SET, dummyZone, (byte) 0x00));
                } else {
                    logger.debug("Received a ZONE STATUS channel command with a non OnOffType: {}", command);
                }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Initializes the handler. This initialization will read/validate the configuration, then will create the
     * {@link SocketSession} and will attempt to connect via {@link #connect()}.
     */
    @Override
    public void initialize() {
        final RNetSystemConfig rnetConfig = getRNetConfig();

        if (rnetConfig == null) {
            return;
        }

        if (rnetConfig.getConnectionString() == null || rnetConfig.getConnectionString().trim().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Connection string to Russound is missing from configuration");
            return;
        }

        initiateConnection(rnetConfig);

        // Try initial connection in a scheduled task
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }

        }, 10, TimeUnit.MILLISECONDS);
    }

    private void initiateConnection(final RNetSystemConfig rnetConfig) {

        RNetInputStreamParser streamParser = new RNetInputStreamParser(new InputHander() {
            @Override
            public void handle(Byte[] bytes) {
                for (BusParser parser : busParsers) {
                    if (parser.matches(bytes)) {
                        ZoneStateUpdate updates = parser.process(bytes);
                        Optional<Thing> zone = getChildThing(RNetConstants.THING_TYPE_RNET_ZONE, updates.getZoneId());
                        if (zone.isPresent()) {
                            ((RNetZoneHandler) zone.get().getHandler()).processUpdates(updates.getStateUpdates());
                        }
                    }
                }
            }
        });
        // lets pick between tcp or serial. by convention if connection address starts with /tcp/ then we will be using
        // tcp
        ConnectionProvider connectionProvider;
        if (rnetConfig.getConnectionString().startsWith("/tcp/")) {
            String address = rnetConfig.getConnectionString().substring(5);
            String[] addressParts = address.split(":");
            connectionProvider = new TcpConnectionProvider(addressParts[0], Integer.parseInt(addressParts[1]));

        } else {
            connectionProvider = new SerialConnectionProvider(rnetConfig.getConnectionString());
        }

        try {
            sessionLock.lock();
            session = new DeviceConnection(connectionProvider, streamParser);
            session.setConnectionStateListener(new ConnectionStateListener() {

                @Override
                public void isConnected(boolean value) {
                    logger.debug("received connection notification: {}", value);
                    if (value) {
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            });
        } finally {
            sessionLock.unlock();
        }
    }

    /**
     * Attempts to connect to the system.
     */
    private void connect() {
        String response = "Server is offline - will try to reconnect later";

        sessionLock.lock();
        pingLock.lock();
        try {
            session.connect();
        } catch (Exception e) {
            logger.error("Error connecting: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response);
            reconnect();
            // do nothing
        } finally {
            pingLock.unlock();
            sessionLock.unlock();
        }

    }

    /**
     * Retries the connection attempt - schedules a job in {@link RNetSystemConfig#getRetryPolling()} seconds to
     * call the {@link #connect()} method. If a retry attempt is pending, the request is ignored.
     */
    protected void reconnect() {
        retryConnectionLock.lock();
        try {
            if (retryConnection == null) {
                final RNetSystemConfig rnetConfig = getRNetConfig();
                if (rnetConfig != null) {

                    logger.info("Will try to reconnect in {} seconds", rnetConfig.getRetryPolling());
                    retryConnection = this.scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            retryConnection = null;
                            try {
                                if (getThing().getStatus() != ThingStatus.ONLINE) {
                                    connect();
                                }
                            } catch (Exception e) {
                                logger.error("Exception connecting: {}", e.getMessage(), e);
                            }
                        }

                    }, rnetConfig.getRetryPolling(), TimeUnit.SECONDS);
                }
            } else {
                logger.debug("RetryConnection called when a retry connection is pending - ignoring request");
            }
        } finally {
            retryConnectionLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Attempts to disconnect from the session. The protocol handler will be set to null, the {@link #ping} will be
     * cancelled/set to null and the {@link #session} will be disconnected
     */
    protected void disconnect() {
        // Cancel ping
        pingLock.lock();
        try {
            if (ping != null) {
                ping.cancel(true);
                ping = null;
            }
        } finally {
            pingLock.unlock();
        }
        sessionLock.lock();
        try {
            session.disconnect();
            session = null;
        } finally {
            sessionLock.unlock();
        }
    }

    /**
     * Simple gets the {@link RNetSystemConfig} from the {@link Thing} and will set the status to offline if not
     * found.
     *
     * @return a possible null {@link RNetSystemConfig}
     */
    public RNetSystemConfig getRNetConfig() {
        configLock.lock();
        try {
            final RNetSystemConfig sysConfig = getThing().getConfiguration().as(RNetSystemConfig.class);

            if (sysConfig == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration file missing");
            }
            return sysConfig;
        } finally {
            configLock.unlock();
        }
    }

    public void sendCommand(Byte[] command) {
        try {
            session.sendCommand(ArrayUtils.toPrimitive(addChecksumandTerminator(command)));
        } catch (NoConnectionException e) {
            logger.debug("received no connection exception", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

    }

    private Byte[] addChecksumandTerminator(Byte[] command) {
        Byte[] commandWithChecksumandTerminator = Arrays.copyOf(command, command.length + 2);
        commandWithChecksumandTerminator[commandWithChecksumandTerminator.length - 2] = russChecksum(command);
        commandWithChecksumandTerminator[commandWithChecksumandTerminator.length - 1] = (byte) 0xf7;
        return commandWithChecksumandTerminator;
    }

    private byte russChecksum(Byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum = sum + data[i];
        }
        sum = sum + data.length;
        byte checksum = (byte) (sum & 0x007F);
        return checksum;
    }

    private Optional<Thing> getChildThing(ThingTypeUID type, ZoneId zoneId) {
        Bridge bridge = getThing();

        List<Thing> things = bridge.getThings();
        for (Thing thing : things) {
            if (type.equals(thing.getThingTypeUID())) {
                if (((Number) thing.getConfiguration().get(RNetConstants.THING_PROPERTIES_CONTROLLER))
                        .intValue() == zoneId.getControllerId()
                        && ((Number) thing.getConfiguration().get(RNetConstants.THING_PROPERTIES_ZONE))
                                .intValue() == zoneId.getZoneId()) {
                    return Optional.of(thing);
                }
            }
        }
        return Optional.empty();
    }

}
