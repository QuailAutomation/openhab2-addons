package org.openhab.binding.isy.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyInsteonDeviceConfiguration;
import org.openhab.binding.isy.config.IsyProgramConfiguration;
import org.openhab.binding.isy.config.IsyVariableConfiguration;
import org.openhab.binding.isy.handler.IsyBridgeHandler;
import org.openhab.binding.isy.internal.Node;
import org.openhab.binding.isy.internal.NodeAddress;
import org.openhab.binding.isy.internal.OHIsyClient;
import org.openhab.binding.isy.internal.Program;
import org.openhab.binding.isy.internal.Scene;
import org.openhab.binding.isy.internal.VariableType;
import org.openhab.binding.isy.internal.protocol.StateVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class IsyRestDiscoveryService extends AbstractDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(IsyRestDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private IsyBridgeHandler bridgeHandler;
    private Map<String, ThingTypeUID> mMapDeviceTypeThingType;
    private Map<String, ThingTypeUID> discoveredNodeTypeCache;
    private ScheduledFuture<?> discoveryJob;

    /**
     * Creates a IsyDiscoveryService.
     */
    public IsyRestDiscoveryService(IsyBridgeHandler bridgeHandler) {
        super(ImmutableSet.of(new ThingTypeUID(IsyBindingConstants.BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS, false);
        this.bridgeHandler = bridgeHandler;
        this.discoveredNodeTypeCache = new HashMap<String, ThingTypeUID>();
        mMapDeviceTypeThingType = new HashMap<String, ThingTypeUID>();
        mMapDeviceTypeThingType.put("10.01", IsyBindingConstants.MOTION_THING_TYPE);
        mMapDeviceTypeThingType.put("01.20", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.24", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("02.16", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("01.2D", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.0E", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.01", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.00", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.30", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.06", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1A", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.0C", IsyBindingConstants.KEYPADLINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1E", IsyBindingConstants.DIMMER_THING_TYPE);
        // mMapDeviceTypeThingType.put("01.09", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.19", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("21.12", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.02", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1F", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("01.2E", IsyBindingConstants.FANLINC_THING_TYPE);
        mMapDeviceTypeThingType.put("01.3A", IsyBindingConstants.DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("07.00", IsyBindingConstants.GARAGEDOORKIT_THING_TYPE);
        mMapDeviceTypeThingType.put("10.02", IsyBindingConstants.TRIGGERLINC_THING_TYPE);
        mMapDeviceTypeThingType.put("02.2A", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.0A", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.0B", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.1C", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.09", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("01.0E", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.06", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.37", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.08", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.38", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("04.10", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.2F", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.14", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.15", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.2F", IsyBindingConstants.SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("10.08", IsyBindingConstants.LEAKDETECTOR_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1B", IsyBindingConstants.KEYPAD_LINC_6_THING_TYPE);
        mMapDeviceTypeThingType.put("02.2C", IsyBindingConstants.KEYPAD_LINC_6_THING_TYPE);
        mMapDeviceTypeThingType.put("01.41", IsyBindingConstants.KEYPADLINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("01.42", IsyBindingConstants.KEYPAD_LINC_5_THING_TYPE);
        mMapDeviceTypeThingType.put("00.05", IsyBindingConstants.REMOTELINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("00.12", IsyBindingConstants.REMOTELINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1C", IsyBindingConstants.KEYPADLINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("01.41", IsyBindingConstants.KEYPADLINC_8_THING_TYPE);
        mMapDeviceTypeThingType.put("01.1A", IsyBindingConstants.INLINELINC_SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.10", IsyBindingConstants.INLINELINC_SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.1F", IsyBindingConstants.INLINELINC_SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("01.21", IsyBindingConstants.INLINELINC_SWITCH_THING_TYPE);
        mMapDeviceTypeThingType.put("02.08", IsyBindingConstants.OUTLETLINC_DIMMER_THING_TYPE);
        mMapDeviceTypeThingType.put("02.39", IsyBindingConstants.OUTLETLINC_DUAL_THING_TYPE);
        mMapDeviceTypeThingType.put("02.1A", IsyBindingConstants.TOGGLELINC_THING_TYPE);
        mMapDeviceTypeThingType.put("10.11", IsyBindingConstants.HIDDENDOORSENSOR_THING_TYPE);
        mMapDeviceTypeThingType.put("10.0A", IsyBindingConstants.SMOKE_DETECTOR_THING_TYPE);
        mMapDeviceTypeThingType.put("05.03", IsyBindingConstants.VENSTAR_THERMOSTAT_THING_TYPE);
        mMapDeviceTypeThingType.put("03.0D", IsyBindingConstants.EZX10_RF_THING_TYPE);
    }

    public void activate() {
        bridgeHandler.registerDiscoveryService(this);
        super.activate(null);
    }

    /**
     * Deactivates the Discovery Service.
     */
    @Override
    public void deactivate() {
        bridgeHandler.unregisterDiscoveryService();
    }

    public ThingTypeUID getTypeUIDFromISYType(String isyType) {
        return this.mMapDeviceTypeThingType.get(isyType);
    }

    @Override
    protected void startScan() {
        try {
            discoverScenes();
        } catch (Exception e) {
            logger.error("error in discover scenes", e);
        }
        try {
            discoverNodes();
        } catch (Exception e) {
            logger.error("error in discover nodes", e);
        }
        try {
            discoverVariables();
        } catch (Exception e) {
            logger.error("error in discover variables", e);
        }
        try {
            discoverPrograms();
        } catch (Exception e) {
            logger.error("error in discover programs", e);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        // if (this.discoveryJob == null || this.discoveryJob.isCancelled()) {
        // this.discoveryJob = scheduler.scheduleWithFixedDelay(new Runnable() {
        // @Override
        // public void run() {
        // if (bridgeHandler.getInsteonClient() != null) {
        // startScan();
        // }
        // }
        // }, 1, 15, TimeUnit.SECONDS);
        // }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (this.discoveryJob != null && !this.discoveryJob.isCancelled()) {
            this.discoveryJob.cancel(true);
            this.discoveryJob = null;
        }
    }

    private void discoverPrograms() {
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        for (Program program : insteon.getPrograms()) {
            discoverProgram(program);
        }
    }

    /**
     * @param program
     */
    public void discoverProgram(Program program) {
        logger.debug("discovered program: " + program);
        Map<String, Object> properties = new HashMap<>(0);
        properties.put(IsyProgramConfiguration.ID, program.getId());
        properties.put(IsyProgramConfiguration.NAME, program.getName());

        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        ThingTypeUID theThingTypeUid = IsyBindingConstants.PROGRAM_THING_TYPE;
        String thingID = removeInvalidUidChars(program.getId());
        ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(program.getName()).build();
        thingDiscovered(discoveryResult);
    }

    public static String removeInvalidUidChars(String original) {
        return original.replace(" ", "_").replace(":", "");
    }

    private void discoverScenes() {
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        for (Scene scene : insteon.getScenes()) {
            discoverScene(scene);
        }
    }

    /**
     * @param scene
     */
    public void discoverScene(Scene scene) {
        logger.debug("discovered scene, address: {}, name: {}, {} links", scene.address, scene.name,
                scene.links.size());

        Map<String, Object> properties = new HashMap<>(0);
        properties.put(IsyInsteonDeviceConfiguration.ADDRESS, scene.address);
        properties.put(IsyInsteonDeviceConfiguration.NAME, scene.name);
        this.bridgeHandler.getSceneMapper().addSceneConfig(scene);

        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        ThingTypeUID theThingTypeUid = IsyBindingConstants.SCENE_THING_TYPE;
        String thingID = removeInvalidUidChars(scene.address);
        ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(scene.name).build();
        thingDiscovered(discoveryResult);
    }

    public void removeDiscoveredScene(String address) {
        ThingUID bridge = this.bridgeHandler.getThing().getUID();
        ThingTypeUID type = IsyBindingConstants.SCENE_THING_TYPE;
        String id = removeInvalidUidChars(address);
        ThingUID uid = new ThingUID(type, bridge, id);
        thingRemoved(uid);
    }

    private void discoverVariables() {
        discoverVariablesForType(VariableType.INTEGER);
        discoverVariablesForType(VariableType.STATE);
    }

    private void discoverVariablesForType(VariableType variableType) {
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        List<StateVariable> variableList = insteon.getVariableDefinitions(variableType).getStateVariables();
        if (variableList != null) {
            for (StateVariable variable : variableList) {
                discoverVariable(variableType, variable);
            }
        }
    }

    /**
     * @param variableType
     * @param variable
     */
    public void discoverVariable(VariableType variableType, StateVariable variable) {
        logger.debug("discovered variable, id:{}, name: {} ", variable.getId(), variable.getName());
        Map<String, Object> properties = new HashMap<>(0);
        properties.put(IsyVariableConfiguration.ID, variable.getId());
        properties.put(IsyVariableConfiguration.TYPE, variableType.getType());

        String typeAsText = variableType.equals(VariableType.INTEGER) ? "integer" : "state";

        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        ThingTypeUID theThingTypeUid = IsyBindingConstants.VARIABLE_THING_TYPE;
        String thingID = typeAsText + "_" + variable.getId();
        ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(variable.getName()).build();
        thingDiscovered(discoveryResult);
    }

    private void discoverNodes() {
        logger.debug("startScan called for Isy");
        OHIsyClient insteon = this.bridgeHandler.getInsteonClient();
        logger.debug("retrieving nodes");
        List<Node> nodes = insteon.getNodes();
        logger.debug("found nodes(#): " + nodes.size());
        for (Node node : nodes) {
            discoverNode(node);
        }
    }

    /**
     * @param node
     */
    public void discoverNode(Node node) {
        logger.debug("Parsing address: {}", node.getAddress());
        NodeAddress nodeAddress = NodeAddress.parseAddressString(node.getAddress());

        Map<String, Object> properties = new HashMap<>(0);
        properties.put(IsyInsteonDeviceConfiguration.ADDRESS, nodeAddress.toStringNoDeviceId());
        properties.put(IsyInsteonDeviceConfiguration.NAME, node.getName());
        properties.put(IsyInsteonDeviceConfiguration.DEVICEID, node.getTypeReadable());

        if (nodeAddress.getDeviceId() == 1) {
            ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
            ThingTypeUID theThingTypeUid = mMapDeviceTypeThingType.get(node.getTypeReadable());
            if (theThingTypeUid == null) {
                logger.warn("Unsupported insteon node, name: " + node.getName() + ", type: " + node.getTypeReadable()
                        + ", address: " + node.getAddress());
                theThingTypeUid = IsyBindingConstants.UNRECOGNIZED_SWITCH_THING_TYPE;
            }

            String thingID = removeInvalidUidChars(nodeAddress.toStringNoDeviceId());
            this.discoveredNodeTypeCache.put(thingID, theThingTypeUid);
            ThingUID thingUID = new ThingUID(theThingTypeUid, bridgeUID, thingID);
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(node.getName()).build();
            thingDiscovered(discoveryResult);
        }
    }

    public void removedDiscoveredNode(String address) {
        NodeAddress nodeAddress = NodeAddress.parseAddressString(address);
        String id = removeInvalidUidChars(nodeAddress.toStringNoDeviceId());
        ThingTypeUID type = this.discoveredNodeTypeCache.get(id);
        if (type == null) {
            return;
        }
        ThingUID uid = new ThingUID(type, bridgeHandler.getThing().getUID(), id);
        thingRemoved(uid);
    }
}
