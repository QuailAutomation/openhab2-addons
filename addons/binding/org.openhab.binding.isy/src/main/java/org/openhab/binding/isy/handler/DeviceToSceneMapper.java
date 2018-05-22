/**
 *
 */
package org.openhab.binding.isy.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.isy.internal.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the linkage between scenes and its associates devices<br>
 * <br>
 * There are two linkages:<br>
 * <ul>
 * <li>scene config: the links to devices a scene contains. Stored as relationship of a scene id to a list of
 * device ids</li>
 * <li>device to scene handler: this maps a device id to a set of OHAB scene ThingHandlers. The handler may
 * effect scene state change when the device changes state</li>
 * </ul>
 *
 * The scene ThingHandler is the actual handler associated with the scene Thing. This class cannot hold on to the
 * handlers longer than the thing/handler lives.<br>
 *
 * @author thomashentschel
 */
public class DeviceToSceneMapper {

    private Logger logger = LoggerFactory.getLogger(DeviceToSceneMapper.class);

    private Map<String, Set<SceneHandler>> device2SceneHandlerMap;
    private Map<String, List<String>> sceneConfigMap;
    private IsyBridgeHandler bridgeHandler;

    /**
     * Instantiate
     *
     * @param bridgeHandler
     */
    public DeviceToSceneMapper(IsyBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
        this.sceneConfigMap = new HashMap<String, List<String>>();
        this.device2SceneHandlerMap = new HashMap<String, Set<SceneHandler>>();
    }

    /**
     * Maps a Scene Thinghandler to a list of devices (device id's) <br>
     * <br>
     * This is called by the initialization routine of each Scene Thinghandler with
     * the scene's links as second argument<br>
     *
     * @param sceneHandler the scene TingHandler instance to link
     * @param deviceIDs the device links as device ID's
     */
    public synchronized void mapScene2Devices(SceneHandler sceneHandler, List<String> deviceIDs) {
        for (String deviceID : deviceIDs) {
            if (!this.device2SceneHandlerMap.containsKey(deviceID)) {
                this.device2SceneHandlerMap.put(deviceID, new HashSet<SceneHandler>());
            }
            Set<SceneHandler> handlers = this.device2SceneHandlerMap.get(deviceID);
            handlers.add(sceneHandler);
        }
    }

    /**
     * remove the scene handler from any device id -> list[scene handler] mappings
     *
     * @param sceneHandler the scene handler to remove
     */
    public synchronized void removeScene(SceneHandler sceneHandler) {

        ThingUID sceneUID = sceneHandler.getThing().getUID();
        for (Set<SceneHandler> handlers : this.device2SceneHandlerMap.values()) {
            Iterator<SceneHandler> it = handlers.iterator();
            while (it.hasNext()) {
                SceneHandler handler = it.next();
                ThingUID currentUID = handler.getThing().getUID();
                if (currentUID.equals(sceneUID)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Return the set of scene ThingHandlers that respond to a device ID.<br>
     * <br>
     * If the current device to scene handler mapping doesn't have a result,
     * this will attempt to look up the scene ThingHandler(s) from the bridge
     *
     * @param deviceID the device to get the scene ThingHandler for
     * @return a set of scene handlers associated with the given device
     */
    public synchronized Set<SceneHandler> getSceneHandlerFor(String deviceID) {

        Set<SceneHandler> result = this.device2SceneHandlerMap.get(deviceID);
        if (result == null) {
            logger.debug("SceneHandler getSceneHandlerFor: handler mapping not present, attempting lookup for {}",
                    deviceID);

            for (Map.Entry<String, List<String>> scene : this.sceneConfigMap.entrySet()) {
                String sceneID = scene.getKey();
                List<String> linkIDS = scene.getValue();
                if (linkIDS != null) {
                    for (String link : linkIDS) {
                        if (link.equals(deviceID)) {
                            logger.debug("getSceneHandlerFor: found scene ID {} for link {}", sceneID, deviceID);
                            ThingHandler handler = this.bridgeHandler.getHandlerForInsteonAddress(sceneID);
                            if (handler != null && handler instanceof SceneHandler) {
                                logger.debug("getSceneHandlerFor: found handler for scene ID {}, linking... ", sceneID);
                                List<String> li = new ArrayList<String>();
                                li.add(deviceID);
                                this.mapScene2Devices((SceneHandler) handler, li);
                            }
                        }
                    }
                } else {
                    logger.debug("getSceneHandlerFor: link ID's NULL/empty for scene ID {} ?", sceneID);
                }
            }
        }
        return this.device2SceneHandlerMap.get(deviceID);
    }

    /**
     * add a list of links to a scene. If this scene config already exists, it will be overwritten by the new config
     *
     * @param sceneID the scene id to create/modify
     * @param rawLinks the links that are part of the scene (in raw form, including device id)
     */
    public synchronized void addSceneConfig(Scene scene) {
        // string are in raw format, with device ID at the end
        List<String> links = new ArrayList<String>();
        links.addAll(scene.links);
        this.sceneConfigMap.put(scene.address, links);
    }

    /**
     * add a scene link to the scene config
     *
     * @param sceneID the scene id to add the link to
     * @param rawLink the ISY address to add as link to scene (in raw form, with device id)
     */
    public synchronized void addSceneLink(String sceneID, String link) {
        List<String> links = this.sceneConfigMap.get(sceneID);
        if (links == null) {
            links = new ArrayList<String>();
            this.sceneConfigMap.put(sceneID, links);
        }
        links.add(link);
    }

    public synchronized List<String> getSceneConfig(String sceneID) {
        return this.sceneConfigMap.get(sceneID);
    }

    /**
     * remove scene config (mapping scene ID -> list[ device ID])
     *
     * @param sceneID
     */
    public synchronized void removeSceneConfig(String sceneID) {
        this.sceneConfigMap.remove(sceneID);
    }

    /**
     * remove link from scene (including scene handler mapped to the device id)
     *
     * @param sceneID the scene id to remove the link from
     * @param link the link to remove, as ISY link address w/o device id
     */
    public synchronized void removeLinkFromScene(String sceneID, String link) {
        List<String> links = this.getSceneConfig(sceneID);
        if (links != null) {
            links.remove(link);
        }
        Set<SceneHandler> handlers = this.device2SceneHandlerMap.get(link);
        if (handlers != null) {
            Iterator<SceneHandler> it = handlers.iterator();
            while (it.hasNext()) {
                SceneHandler handler = it.next();
                ThingUID currentSceneID = handler.getThing().getUID();
                if (currentSceneID.getAsString().contains(sceneID)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * remove all traces of scene linkage to this device address<br>
     * <ul>
     * <li>from scene config ( scene id -> list[device addresses] )</li>
     * <li>from scene handler mapping ( device id -> list[isy scene handler] )</li>
     * </ul>
     *
     * @param rawAddr the raw insteon address to the address + device id
     */
    public synchronized void removeDeviceLinks(String rawAddr) {
        // go thru all deviceIDs in scene config
        // find rawAddr in device IDs and remove
        for (List<String> deviceIDs : sceneConfigMap.values()) {
            Iterator<String> deviceID = deviceIDs.iterator();
            while (deviceID.hasNext()) {
                String id = deviceID.next();
                if (id.equals(rawAddr)) {
                    deviceID.remove();
                }
            }
        }
        // .. and remove all handlers for that device
        this.device2SceneHandlerMap.remove(rawAddr);
    }
}
