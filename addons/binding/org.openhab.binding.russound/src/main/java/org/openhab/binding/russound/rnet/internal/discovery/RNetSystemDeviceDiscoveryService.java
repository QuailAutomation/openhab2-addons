/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.rnet.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.russound.rnet.handler.RNetSystemHandler;
import org.openhab.binding.russound.rnet.internal.RNetConstants;
import org.openhab.binding.russound.rnet.internal.RNetSystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery for RNET devices
 * 
 * @author craig hamilton
 *
 */
public class RNetSystemDeviceDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(RNetSystemDeviceDiscoveryService.class);

    private RNetSystemHandler sysHandler;

    public RNetSystemDeviceDiscoveryService(RNetSystemHandler handler) {
        super(RNetConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, 30, false);
        if (handler == null) {
            throw new IllegalArgumentException("sysHandler can't be null");
        }
        this.sysHandler = handler;
    }

    @Override
    protected void startScan() {
        final RNetSystemConfig sysConfig = this.sysHandler.getThing().getConfiguration().as(RNetSystemConfig.class);

        logger.debug("Should start scan for RNet");
        // for now, lets assume can have up to 6 controllers, and up to 6 zones in each
        for (int controllerNumber = 1; controllerNumber <= sysConfig.getNumControllers(); controllerNumber++) {
            for (int zoneNumber = 1; zoneNumber <= sysConfig.getZonesPer(); zoneNumber++) {
                // let's request zone info for each combination. If we receive a valid return message, let's add the
                // device(s)
                logger.debug("create a zone with controller id: {}, zone id: {}", controllerNumber, zoneNumber);
                Map<String, Object> properties = new HashMap<>();
                properties.put(RNetConstants.THING_PROPERTIES_CONTROLLER, controllerNumber);
                properties.put(RNetConstants.THING_PROPERTIES_ZONE, zoneNumber);

                String id = String.format("%d_%d", controllerNumber, zoneNumber);
                String name = String.format("RNet Audio Zone (%s)", id);
                ThingUID thingUID = new ThingUID(RNetConstants.THING_TYPE_RNET_ZONE, id,
                        sysHandler.getThing().getUID().getId());
                thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(sysHandler.getThing().getUID())
                        .withLabel(name).withProperties(properties).build());

            }
        }

    }

}
