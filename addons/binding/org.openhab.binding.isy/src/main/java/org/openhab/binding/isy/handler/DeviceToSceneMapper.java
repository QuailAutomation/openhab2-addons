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
 * @author thomashentschel
 *
 */
public class DeviceToSceneMapper {

    private Logger logger = LoggerFactory.getLogger(DeviceToSceneMapper.class);

    private Map<String, Set<SceneHandler>> device2SceneHandlerMap;
    private Map<String, List<String>> sceneConfigMap;
    private IsyBridgeHandler bridgeHandler;

    /**
     * @param bridgeHandler
     *
     */
    public DeviceToSceneMapper(IsyBridgeHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;
        this.sceneConfigMap = new HashMap<String, List<String>>();
        this.device2SceneHandlerMap = new HashMap<String, Set<SceneHandler>>();
    }

    public synchronized void mapScene2Devices(SceneHandler sceneHandler, List<String> deviceIDs) {
        for (String deviceID : deviceIDs) {
            if (!this.device2SceneHandlerMap.containsKey(deviceID)) {
                this.device2SceneHandlerMap.put(deviceID, new HashSet<SceneHandler>());
            }
            Set<SceneHandler> handlers = this.device2SceneHandlerMap.get(deviceID);
            handlers.add(sceneHandler);
        }
    }

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
}
