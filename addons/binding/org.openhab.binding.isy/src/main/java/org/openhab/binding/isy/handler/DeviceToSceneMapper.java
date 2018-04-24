/**
 *
 */
package org.openhab.binding.isy.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.binding.ThingHandler;
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
        for (String deviceID : this.device2SceneHandlerMap.keySet()) {
            Set<SceneHandler> handlers = this.device2SceneHandlerMap.get(deviceID);
            List<SceneHandler> removals = new ArrayList<SceneHandler>();
            for (SceneHandler handler : handlers) {
                if (handler.equals(sceneHandler)) {
                    removals.add(handler);
                }
            }
            for (SceneHandler removal : removals) {
                handlers.remove(removal);
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

    public synchronized void addSceneConfig(String sceneID, List<String> links) {
        this.sceneConfigMap.put(sceneID, links);
    }

    public synchronized List<String> getSceneConfig(String sceneID) {
        return this.sceneConfigMap.get(sceneID);
    }

}
