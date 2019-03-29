/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.meteostick.internal;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.meteostick.internal.handler.MeteostickBridgeHandler;
import org.openhab.binding.meteostick.internal.handler.MeteostickSensorHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteostickHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.meteostick")
public class MeteostickHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(MeteostickHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MeteostickBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)
                | MeteostickSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.debug("MeteoStick thing factory: createHandler {} of type {}", thing.getThingTypeUID(), thing.getUID());

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (MeteostickBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new MeteostickBridgeHandler((Bridge) thing);
        }

        if (MeteostickSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new MeteostickSensorHandler(thing);
        }

        return null;
    }
}
