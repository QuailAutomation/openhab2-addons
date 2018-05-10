/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.eagle200.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.eagle200.Eagle200BindingConstants;
import org.openhab.binding.eagle200.discovery.EagleDiscoveryService;
import org.openhab.binding.eagle200.handler.Eagle200BridgeHandler;
import org.openhab.binding.eagle200.handler.Eagle200MeterHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Eagle200HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.eagle200")
@NonNullByDefault
public class Eagle200HandlerFactory extends BaseThingHandlerFactory {

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<ThingUID, ServiceRegistration<?>>();

    private static final Logger logger = LoggerFactory.getLogger(Eagle200HandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return Eagle200BindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Eagle200BindingConstants.THING_TYPE_EAGLE200_BRIDGE.equals(thingTypeUID)) {
            Eagle200BridgeHandler handler = new Eagle200BridgeHandler((Bridge) thing);
            this.registerBridgeDiscoveryService(handler);
            return handler;
        }

        if (Eagle200BindingConstants.THING_TYPE_EAGLE200_METER.equals(thingTypeUID)) {
            Eagle200MeterHandler handler = new Eagle200MeterHandler(thing);
            return handler;
        }

        return null;
    }

    /**
     * Register the Thing Discovery Service for a bridge.
     *
     * @param bridgeHandler
     */
    private void registerBridgeDiscoveryService(Eagle200BridgeHandler bridgeHandler) {
        logger.debug("Eagle discovery service activating");

        EagleDiscoveryService discoveryService = new EagleDiscoveryService(bridgeHandler);
        discoveryService.activate();

        ServiceRegistration<?> registration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<String, Object>());

        this.discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), registration);

        logger.debug("registerBridgeDiscoveryService(): Bridge Handler - {}, Class Name - {}, Discovery Service - {}",
                bridgeHandler, DiscoveryService.class.getName(), discoveryService);
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof Eagle200BridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegistrations
                    .get(thingHandler.getThing().getUID());
            // remove discovery service, if bridge handler is removed
            EagleDiscoveryService service = (EagleDiscoveryService) bundleContext.getService(serviceReg.getReference());
            if (service != null) {
                service.deactivate();
            }
            serviceReg.unregister();
            discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
            logger.debug("Eagle discovery service removed");
        }
    }
}
