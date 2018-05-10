package org.openhab.binding.isy.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.isy.config.IsySceneConfiguration;
import org.openhab.binding.isy.internal.protocol.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SceneHandler extends AbtractIsyThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(SceneHandler.class);
    private Map<String, OnOffType> linkStates;

    public SceneHandler(Thing thing) {
        super(thing);
        this.linkStates = new HashMap<String, OnOffType>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        IsyBridgeHandler bridgeHandler = getBridgeHandler();
        IsySceneConfiguration scene = getThing().getConfiguration().as(IsySceneConfiguration.class);
        if (command.equals(RefreshType.REFRESH)) {
            logger.debug("SceneHandler handleCommand: REFRESH for chan: {}", channelUID);
            this.updateState(channelUID, this.getSceneState(true));
        } else if (OnOffType.ON.equals(command)) {
            logger.debug("SceneHandler handleCommand: ON for chan: {}", channelUID);
            bridgeHandler.getInsteonClient().changeSceneState(scene.address, 255);
        } else if (OnOffType.OFF.equals(command)) {
            logger.debug("SceneHandler handleCommand: OFF for chan: {}", channelUID);
            bridgeHandler.getInsteonClient().changeSceneState(scene.address, 0);
        } else {
            logger.warn("Unexpected command: " + command.toFullString());
        }
    }

    public void handleUpdate(String control, String action, String node) {

        logger.debug("SceneHandler handleUpdate: control: {} , action: {} , node:{}", control, action, node);

        OnOffType newState = null;
        if ("ST".equals(control)) {
            int newIntState = Integer.parseInt(action);
            // if a linked item is not set to 0, it's ON
            if (newIntState == 0) {
                newState = OnOffType.OFF;
            } else {
                newState = OnOffType.ON;
            }
        } else if ("DOF".equals(control) || "DFOF".equals(control)) {
            newState = OnOffType.OFF;
        } else if ("DON".equals(control) || "DFON".equals(control)) {
            newState = OnOffType.OFF;
        } else {
            return;
        }

        OnOffType last = this.getSceneState(false);
        logger.debug("SceneHandler handleUpdate: update for {} is {}", node, newState.toFullString());
        this.updateLinkState(node, newState);
        OnOffType now = this.getSceneState(false);

        logger.debug("SceneHandler handleUpdate: check channel update needed? last: {}, now {}", last, now);
        if (now != last) {
            for (Channel chan : this.getThing().getChannels()) {
                this.updateState(chan.getUID(), newState);
            }
        }
    }

    private OnOffType getSceneState(boolean forceUpdate) {

        if (forceUpdate) {
            this.checkSceneUpdateNeeded();
        }

        // first: if any of the links is null, the result is invalid
        for (OnOffType value : this.linkStates.values()) {
            if (value == null) {
                return null;
            }
        }

        // 2nd: if any of the links is ON, the scene is ON
        for (OnOffType value : this.linkStates.values()) {
            if (OnOffType.ON.equals(value)) {
                return OnOffType.ON;
            }
        }
        // if nothing null and nothing ON, then it's OFF
        return OnOffType.OFF;
    }

    private void checkSceneUpdateNeeded() {
        Map<String, OnOffType> updates = new HashMap<String, OnOffType>();

        // first collect all required updates
        for (Map.Entry<String, OnOffType> entry : this.linkStates.entrySet()) {
            // if the value for the link is still null, that means there was no update for that link from
            // the web socket yet. Go fetch the value from the REST interface
            if (entry.getValue() == null) {
                String address = entry.getKey();
                Property update = this.getBridgeHandler().getInsteonClient().getNodeStatus(address);
                if (update != null) {
                    OnOffType newState = null;
                    int newIntState = 0;
                    if ("DOF".equals(update.getValue()) || "DFOF".equals(update.getValue())) {
                        newIntState = 0;
                    } else if ("DON".equals(update.getValue()) || "DFON".equals(update.getValue())) {
                        newIntState = 255;
                    } else {
                        String number = update.getValue();
                        if (number != null) {
                            number = number.trim();
                            if (!number.isEmpty()) {
                                newIntState = Integer.parseInt(update.getValue());
                            } else {
                                // every once in a while, the isy sends a empty string instead of the value
                                // not sure what to do in that case
                                newIntState = 0;
                            }
                        }
                    }
                    // if a linked item is not set to 0, it's ON
                    if (newIntState == 0) {
                        newState = OnOffType.OFF;
                    } else {
                        newState = OnOffType.ON;
                    }
                    logger.debug("SceneHandler checkSceneUpdateNeeded: requested ISY state update for {} is {}",
                            address, newState.toFullString());
                    updates.put(address, newState);
                } else {
                    logger.warn("requested ISY state update for {} was NULL", address);
                }
            }
        }
        // update the instance map
        for (Map.Entry<String, OnOffType> entry : updates.entrySet()) {
            this.updateLinkState(entry.getKey(), entry.getValue());
        }
    }

    private void updateLinkState(String node, OnOffType state) {
        this.linkStates.put(node, state);
    }

    @Override
    public void initialize() {

        IsySceneConfiguration sceneConfig = this.getThing().getConfiguration().as(IsySceneConfiguration.class);
        IsyBridgeHandler bridge = this.getBridgeHandler();
        ThingStatus status = bridge.getThing().getStatus();
        logger.debug("SceneHandler initialize: Bridge status is {}", status);
        if (ThingStatus.ONLINE.equals(status)) {
            // if bridge already online, then should have the required info
            List<String> links = bridge.getSceneMapper().getSceneConfig(sceneConfig.address);
            if (links != null && !links.isEmpty()) {
                // create initial link states as null, this way we can figure if it needs a initial request later
                for (String link : links) {
                    logger.debug("SceneHandler initialize: initializing link status for {}", link);
                    this.linkStates.put(link, null);
                }
                // point mapper to this handler for each link address
                bridge.getSceneMapper().mapScene2Devices(this, links);
            }
            this.updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(@NonNull ThingStatusInfo bridgeStatusInfo) {
        logger.debug("SceneHandler bridgeStatusChanged: Bridge status changed to {}", bridgeStatusInfo.getStatus());
        if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())) {
            IsySceneConfiguration sceneConfig = this.getThing().getConfiguration().as(IsySceneConfiguration.class);
            IsyBridgeHandler bridge = this.getBridgeHandler();
            List<String> links = bridge.getSceneMapper().getSceneConfig(sceneConfig.address);
            if (links != null && !links.isEmpty()) {
                // create initial link states as null, this way we can figure if it needs a initial request later
                for (String link : links) {
                    logger.debug("SceneHandler bridgeStatusChanged: initializing link status for {}", link);
                    this.linkStates.put(link, null);
                }
                // point mapper to this handler for each link address
                bridge.getSceneMapper().mapScene2Devices(this, links);
            }
            this.updateStatus(ThingStatus.ONLINE);
        } else if (ThingStatus.OFFLINE.equals(bridgeStatusInfo.getStatus())) {
            this.linkStates.clear();
            this.getBridgeHandler().getSceneMapper().removeScene(this);
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    @Override
    public void handleRemoval() {
        this.linkStates.clear();
        this.getBridgeHandler().getSceneMapper().removeScene(this);
        super.handleRemoval();
    }

    @Override
    public void handleConfigurationUpdate(@NonNull Map<@NonNull String, @NonNull Object> configurationParameters) {
        // TODO Auto-generated method stub
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void dispose() {
        this.linkStates.clear();
        super.dispose();
    }

}
