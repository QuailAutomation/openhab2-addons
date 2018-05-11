package org.openhab.binding.isy.handler;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.ScanListener;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyBridgeConfiguration;
import org.openhab.binding.isy.config.IsyInsteonDeviceConfiguration;
import org.openhab.binding.isy.discovery.IsyRestDiscoveryService;
import org.openhab.binding.isy.internal.ISYModelChangeListener;
import org.openhab.binding.isy.internal.InsteonClientProvider;
import org.openhab.binding.isy.internal.IsyRestClient;
import org.openhab.binding.isy.internal.IsyWebSocketSubscription;
import org.openhab.binding.isy.internal.NodeAddress;
import org.openhab.binding.isy.internal.OHIsyClient;
import org.openhab.binding.isy.internal.Scene;
import org.openhab.binding.isy.internal.VariableType;
import org.openhab.binding.isy.internal.protocol.Event;
import org.openhab.binding.isy.internal.protocol.EventInfo;
import org.openhab.binding.isy.internal.protocol.EventNode;
import org.openhab.binding.isy.internal.protocol.Node;
import org.openhab.binding.isy.internal.protocol.NodeInfo;
import org.openhab.binding.isy.internal.protocol.Nodes;
import org.openhab.binding.isy.internal.protocol.Properties;
import org.openhab.binding.isy.internal.protocol.Property;
import org.openhab.binding.isy.internal.protocol.StateVariable;
import org.openhab.binding.isy.internal.protocol.SubscriptionResponse;
import org.openhab.binding.isy.internal.protocol.VariableEvent;
import org.openhab.binding.isy.internal.protocol.VariableList;
import org.openhab.binding.isy.internal.protocol.elk.Area;
import org.openhab.binding.isy.internal.protocol.elk.AreaEvent;
import org.openhab.binding.isy.internal.protocol.elk.Areas;
import org.openhab.binding.isy.internal.protocol.elk.ElkStatus;
import org.openhab.binding.isy.internal.protocol.elk.Topology;
import org.openhab.binding.isy.internal.protocol.elk.Zone;
import org.openhab.binding.isy.internal.protocol.elk.ZoneEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class IsyBridgeHandler extends BaseBridgeHandler implements InsteonClientProvider {
    private String testXmlVariableUpdate = "<?xml version=\"1.0\"?><Event seqnum=\"1607\" sid=\"uuid:74\"><control>_1</control><action>6</action><node></node><eventInfo><var type=\"2\" id=\"3\"><val>0</val><ts>20170718 09:16:26</ts></var></eventInfo></Event>";
    private String testXmlNodeUpdate = "<?xml version=\"1.0\"?><Event seqnum=\"1602\" sid=\"uuid:74\"><control>ST</control><action>255</action><node>28 C1 F3 1</node><eventInfo></eventInfo></Event>";
    private Logger logger = LoggerFactory.getLogger(IsyBridgeHandler.class);

    private IsyRestDiscoveryService bridgeDiscoveryService;

    private IsyRestClient isyClient;

    private IsyWebSocketSubscription eventSubscriber;
    /*
     * Responsible for subscribing to isy for events
     */

    private XStream xStream;
    private DeviceToSceneMapper sceneMapper;
    private ScheduledFuture<?> discoverTask = null;

    public IsyBridgeHandler(Bridge bridge) {
        super(bridge);

        xStream = new XStream(new StaxDriver());
        xStream.ignoreUnknownElements();
        xStream.setClassLoader(IsyRestDiscoveryService.class.getClassLoader());
        xStream.processAnnotations(new Class[] { Properties.class, Property.class, Event.class, EventInfo.class,
                EventNode.class, ZoneEvent.class, AreaEvent.class, VariableList.class, StateVariable.class,
                VariableEvent.class, SubscriptionResponse.class, Topology.class, Zone.class, ElkStatus.class,
                Areas.class, Area.class, Node.class, Nodes.class, NodeInfo.class });

        this.sceneMapper = new DeviceToSceneMapper(this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("isy bridge handler called");
    }

    @Override
    public void dispose() {
        logger.trace("Dispose called");
        if (this.eventSubscriber != null) {
            eventSubscriber.disconnect();
            eventSubscriber = null;
        }
        if (this.discoverTask != null && !this.discoverTask.isCancelled()) {
            this.discoverTask.cancel(true);
            this.discoverTask = null;
        }
    }

    private IsyVariableHandler getVariableHandler(VariableType type, int id) {
        logger.debug("find thing handler for id: {}, type: {}", id, type.getType());
        for (Thing thing : getThing().getThings()) {
            if (IsyBindingConstants.VARIABLE_THING_TYPE.equals(thing.getThingTypeUID())) {
                int theId = ((BigDecimal) thing.getConfiguration().get("id")).intValue();
                int theType = ((BigDecimal) thing.getConfiguration().get("type")).intValue();
                logger.trace("checking thing to see if match, id: {} , type: {}", theId, theType);
                if (theType == type.getType() && theId == id) {
                    return (IsyVariableHandler) thing.getHandler();
                }
            }
        }
        return null;
    }

    @Override
    public void initialize() {
        logger.debug("initialize called for bridge handler");

        IsyBridgeConfiguration config = getThing().getConfiguration().as(IsyBridgeConfiguration.class);

        String usernameAndPassword = config.getUser() + ":" + config.getPassword();
        String authorizationHeaderValue = "Basic "
                + java.util.Base64.getEncoder().encodeToString(usernameAndPassword.getBytes());
        this.isyClient = new IsyRestClient(config.getIpAddress(), authorizationHeaderValue, xStream);
        ISYModelChangeListener modelListener = new IsyListener();
        this.eventSubscriber = new IsyWebSocketSubscription(config.getIpAddress(), authorizationHeaderValue,
                modelListener, xStream);

        Runnable discover = new Runnable() {
            @Override
            public void run() {
                try {
                    // initialize mapping scene links to scene addresses. Do this before starting web service
                    // this way we get the initial set of updates for scenes too
                    // this is also the first statement that can fail b/c of network or config (hostname/login) error
                    List<Scene> scenes = IsyBridgeHandler.this.isyClient.getScenes();
                    for (Scene scene : scenes) {
                        IsyBridgeHandler.this.getSceneMapper().addSceneConfig(scene);
                    }

                    // start a scan of the REST interface on the ISY to get the device structure
                    // and a initial value update. Register a listener to figure when that scna is done
                    IsyBridgeHandler.this.bridgeDiscoveryService.startScan(new ScanListener() {

                        // the REST scan is finished, time to connect the WS for live updates.
                        // If that goes ok, time to go ONLINE
                        @Override
                        public void onFinished() {
                            try {
                                eventSubscriber.connect();
                                updateStatus(ThingStatus.ONLINE);
                                discoverTask.cancel(true);
                            } catch (Exception e) {
                                logger.debug("ISY bridge initialize: web socket connect failed: {}", e.getMessage());
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                            }
                        }

                        // the REST scan failed. Keep the discovery task going
                        @Override
                        public void onErrorOccurred(@Nullable Exception e) {
                            logger.debug("ISY bridge initialize: discovery scan failed: {}",
                                    e != null ? e.getMessage() : "(no exception)");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        }
                    });

                    // most likely caused by network problem or config error attempting to get scenes
                    // possible that the REST scan parsing failed too
                } catch (Exception e) {
                    logger.debug("ISY bridge init failed: {}", e.getMessage());
                    IsyBridgeHandler.this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                }
            }
        };

        // this task is cancelled once the bridge goes ONLINE (REST scan + WS connect success), otherwise keep trying...
        this.discoverTask = this.scheduler.scheduleWithFixedDelay(discover, 0, 60, TimeUnit.SECONDS);
    }

    public void registerDiscoveryService(DiscoveryService isyBridgeDiscoveryService) {
        this.bridgeDiscoveryService = (IsyRestDiscoveryService) isyBridgeDiscoveryService;
    }

    public void unregisterDiscoveryService() {
        this.bridgeDiscoveryService = null;
    }

    private IsyDeviceHandler getThingHandler(String address) {
        logger.trace("find thing handler for address: {}", address);
        if (!address.startsWith("n")) {
            String addressNoDeviceId = NodeAddress.stripDeviceId(address);
            logger.trace("Find thing for address: {}", addressNoDeviceId);
            for (Thing thing : getThing().getThings()) {
                if (!(IsyBindingConstants.PROGRAM_THING_TYPE.equals(thing.getThingTypeUID())
                        || IsyBindingConstants.VARIABLE_THING_TYPE.equals(thing.getThingTypeUID())
                        || IsyBindingConstants.SCENE_THING_TYPE.equals(thing.getThingTypeUID()))) {

                    String theAddress = (String) thing.getConfiguration().get("address");

                    if (theAddress != null) {
                        String thingsAddress = NodeAddress.stripDeviceId(theAddress);
                        if (addressNoDeviceId.equals(thingsAddress)) {
                            logger.trace("address: {}", thingsAddress);
                            return (IsyDeviceHandler) thing.getHandler();
                        }
                    }
                }
            }

            logger.debug("No thing discovered for address: {}", address);
        } else {
            logger.debug("Did not return thing handler because detected polygot node: {}", address);
        }

        return null;
    }

    @Override
    public OHIsyClient getInsteonClient() {
        return isyClient;
    }

    public DeviceToSceneMapper getSceneMapper() {
        return this.sceneMapper;
    }

    ThingHandler getHandlerForInsteonAddress(String address) {
        logger.debug("getHandlerForInsteonAddress: trying address {}", address);
        Bridge bridge = this.getThing();
        Iterator<Thing> things = bridge.getThings().iterator();
        while (things.hasNext()) {
            Thing thing = things.next();
            if (!(thing.getHandler() instanceof IsyVariableHandler)) {
                IsyInsteonDeviceConfiguration config = thing.getConfiguration().as(IsyInsteonDeviceConfiguration.class);
                logger.trace("getHandlerForInsteonAddress: got config address {}", config.address);
                if (address.equals(config.address)) {
                    logger.debug("getHandlerForInsteonAddress: found handler for address {}", address);
                    return thing.getHandler();
                }
            }
        }
        logger.debug("getHandlerForInsteonAddress: no handler found for address {}", address);
        return null;
    }

    class IsyListener implements ISYModelChangeListener {

        @Override
        public void onDeviceOnLine() {
            logger.debug("Received onDeviceOnLine message");
            updateStatus(ThingStatus.ONLINE);
        }

        @Override
        public void onDeviceOffLine() {
            logger.debug("Received onDeviceOffLine message");
            updateStatus(ThingStatus.OFFLINE);
        }

        @Override
        public void onNodeAdded(Event event) {
            String addr = event.getEventInfo().getNode().getAddress();
            String type = event.getEventInfo().getNode().getType();
            String name = event.getEventInfo().getNode().getName();
            org.openhab.binding.isy.internal.Node node = new org.openhab.binding.isy.internal.Node(
                    IsyRestClient.removeBadChars(name), addr, type);
            logger.debug("ISY added node {} [{}], type {}", name, addr, type);
            bridgeDiscoveryService.discoverNode(node);
        }

        @Override
        public void onNodeChanged(Event event) {
            logger.debug("onModelChanged called, node: {}, control: {}, action: {}, var event: {}", event.getNode(),
                    event.getControl(), event.getAction(), event.getEventInfo().getVariableEvent());
            IsyDeviceHandler handler = null;
            Set<SceneHandler> sceneHandlers = null;
            if (!"".equals(event.getNode())) {
                handler = getThingHandler(event.getNode());
                sceneHandlers = sceneMapper.getSceneHandlerFor(event.getNode());
            }
            if (handler != null) {
                handler.handleUpdate(event.getControl(), event.getAction(), event.getNode());
            }
            if (sceneHandlers != null) {
                for (SceneHandler sceneHandler : sceneHandlers) {
                    sceneHandler.handleUpdate(event.getControl(), event.getAction(), event.getNode());
                }
            }
        }

        @Override
        public void onNodeRenamed(Event event) {
            String newname = event.getEventInfo().getNewName();
            NodeAddress nodeAddress = NodeAddress.parseAddressString(event.getNode());
            String id = IsyRestDiscoveryService.removeInvalidUidChars(nodeAddress.toStringNoDeviceId());
            logger.debug("ISY renamed node {} to [{}]", id, newname);
            List<Thing> things = getThing().getThings();
            for (Thing thing : things) {
                String current = thing.getUID().getAsString();
                if (current.contains(id)) {
                    logger.debug("ISY rename for node {} found thing {}", id, current);
                    thing.setLabel(newname);
                    return;
                }
            }
        }

        @Override
        public void onNodeRemoved(Event event) {
            String rawAddr = event.getNode();
            NodeAddress nodeAddress = NodeAddress.parseAddressString(event.getNode());
            String addr = IsyRestDiscoveryService.removeInvalidUidChars(nodeAddress.toStringNoDeviceId());
            logger.debug("ISY removed node {}", rawAddr);

            // remove from inbox
            bridgeDiscoveryService.removedDiscoveredNode(rawAddr);

            // remove device ids from scene mapper linkage
            sceneMapper.removeDeviceLinks(rawAddr);

            // cannot call rest interface at this point, the node is already gone
            // set device status to GONE
            for (Thing thing : getThing().getThings()) {
                String current = thing.getUID().getAsString();
                if (current.contains(addr)) {
                    thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                            "Device was removed from ISY"));
                    logger.debug("ISY remove for node {} found thing {}", addr, current);
                    return;
                }
            }
        }

        @Override
        public void onSceneAdded(Event event) {
            String addr = event.getNode();
            String name = event.getEventInfo().getGroupName();
            Scene scene = new Scene(name, addr, Collections.emptyList());
            logger.debug("ISY added scene {} [{}]", name, addr);
            bridgeDiscoveryService.discoverScene(scene);
        }

        @Override
        public void onSceneLinkAdded(Event event) {
            String sceneID = event.getNode();
            String newLink = event.getEventInfo().getMovedNode();

            logger.debug("ISY added link {} to scene {}", newLink, sceneID);
            sceneMapper.addSceneLink(sceneID, newLink);

            // if the thing already exists (not just in inbox), then update the links
            Thing t = null;
            for (Thing thing : getThing().getThings()) {
                String current = thing.getUID().getAsString();
                if (current.contains(sceneID)) {
                    t = thing;
                    break;
                }
            }
            if (t == null) {
                return;
            }
            ThingHandler handler = t.getHandler();
            if (handler == null) {
                return;
            }

            // reset/init the thing, which re-maps the links in the scene
            logger.debug("ISY added link {} to scene {}, resetting thing {}", newLink, sceneID,
                    t.getUID().getAsString());
            handler.thingUpdated(t);
        }

        @Override
        public void onSceneLinkRemoved(Event event) {
            String sceneID = event.getNode();
            String removedLink = event.getEventInfo().getRemovedNode();
            logger.debug("ISY removed link {} from scene {}", removedLink, sceneID);
            sceneMapper.removeLinkFromScene(sceneID, removedLink);

            Thing t = null;
            for (Thing thing : getThing().getThings()) {
                String current = thing.getUID().getAsString();
                if (current.contains(sceneID)) {
                    t = thing;
                    break;
                }
            }
            if (t == null) {
                return;
            }
            ThingHandler handler = t.getHandler();
            if (handler == null) {
                return;
            }

            // rest/init the scene (need to do this for actual device, since this doesn't do very much?)
            logger.debug("ISY removed link {} from scene {}, resetting thing {}", removedLink, sceneID,
                    t.getUID().getAsString());
            handler.thingUpdated(t);
        }

        @Override
        public void onSceneRemoved(Event event) {
            String id = event.getNode();
            logger.debug("ISY removed scene {}", id);

            // remove from inbox
            bridgeDiscoveryService.removeDiscoveredScene(id);

            // remove from scene mapper
            sceneMapper.removeSceneConfig(id);

            // cannot call rest interface at this point, the node is already gone
            // set status, and remove from scene handler map
            for (Thing thing : getThing().getThings()) {
                String current = thing.getUID().getAsString();
                if (current.contains(id)) {
                    logger.debug("ISY removed scene {} found thing {}", id, current);
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        sceneMapper.removeScene((SceneHandler) handler);
                    }
                    thing.setStatusInfo(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE,
                            "Scene was removed from ISY"));
                    return;
                }
            }
        }

        @Override
        public void onSceneRenamed(Event event) {
            String id = event.getNode();
            String newname = event.getEventInfo().getNewName();
            logger.debug("ISY renamed scene {} to {}", id, newname);
            List<Thing> things = getThing().getThings();
            for (Thing thing : things) {
                String current = thing.getUID().getAsString();
                if (current.contains(id)) {
                    logger.debug("ISY renamed scene {} to {} found thing {}", id, newname, current);
                    thing.setLabel(newname);
                    return;
                }
            }
        }

        @Override
        public void onVariableChanged(VariableEvent event) {
            logger.debug("need to find variable handler, id is: {}, val: {}", event.getId(), event.getVal());
            IsyVariableHandler handler = getVariableHandler(VariableType.fromInt(event.getType()), event.getId());
            if (handler != null) {
                handler.handleUpdate(event.getVal());
            }
        }
    }
}
