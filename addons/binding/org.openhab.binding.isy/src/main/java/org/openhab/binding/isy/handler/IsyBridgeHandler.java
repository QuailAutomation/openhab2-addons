package org.openhab.binding.isy.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyBridgeConfiguration;
import org.openhab.binding.isy.discovery.IsyRestDiscoveryService;
import org.openhab.binding.isy.internal.ISYModelChangeListener;
import org.openhab.binding.isy.internal.InsteonClientProvider;
import org.openhab.binding.isy.internal.IsyRestClient;
import org.openhab.binding.isy.internal.IsyWebSocketSubscription;
import org.openhab.binding.isy.internal.NodeAddress;
import org.openhab.binding.isy.internal.OHIsyClient;
import org.openhab.binding.isy.internal.VariableType;
import org.openhab.binding.isy.internal.protocol.Event;
import org.openhab.binding.isy.internal.protocol.EventInfo;
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

    private DiscoveryService bridgeDiscoveryService;

    private IsyRestClient isyClient;

    private IsyWebSocketSubscription eventSubscriber;
    /*
     * Responsible for subscribing to isy for events
     */

    private XStream xStream;

    public IsyBridgeHandler(Bridge bridge) {
        super(bridge);

        xStream = new XStream(new StaxDriver());
        xStream.ignoreUnknownElements();
        xStream.setClassLoader(IsyRestDiscoveryService.class.getClassLoader());
        xStream.processAnnotations(new Class[] { Properties.class, Property.class, Event.class, EventInfo.class,
                ZoneEvent.class, AreaEvent.class, VariableList.class, StateVariable.class, VariableEvent.class,
                SubscriptionResponse.class, Topology.class, Zone.class, ElkStatus.class, Areas.class, Area.class,
                Node.class, Nodes.class, NodeInfo.class });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("isy bridge handler called");
    }

    @Override
    public void dispose() {
        logger.trace("Dispose called");
        eventSubscriber.disconnect();
        eventSubscriber = null;
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

        eventSubscriber = new IsyWebSocketSubscription(config.getIpAddress(), authorizationHeaderValue,
                new ISYModelChangeListener() {

                    @Override
                    public void onModelChanged(Event event) {
                        logger.debug("onModelChanged called, control: {}, action: {}, var event: {}",
                                event.getControl(), event.getAction(), event.getEventInfo().getVariableEvent());
                        IsyDeviceHandler handler = null;
                        if (!"".equals(event.getNode())) {
                            handler = getThingHandler(event.getNode());
                        }
                        if (handler != null) {
                            handler.handleUpdate(event.getControl(), event.getAction(), event.getNode());
                        }
                    }

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
                    public void onVariableChanged(VariableEvent event) {
                        logger.debug("need to find variable handler, id is: {}, val: {}", event.getId(),
                                event.getVal());
                        IsyVariableHandler handler = getVariableHandler(VariableType.fromInt(event.getType()),
                                event.getId());
                        if (handler != null) {
                            handler.handleUpdate(event.getVal());
                        }
                    }
                }, xStream);
        eventSubscriber.connect();
        isyClient = new IsyRestClient(config.getIpAddress(), authorizationHeaderValue, xStream);
        updateStatus(ThingStatus.ONLINE);

    }

    public void registerDiscoveryService(DiscoveryService isyBridgeDiscoveryService) {
        this.bridgeDiscoveryService = isyBridgeDiscoveryService;

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
}
