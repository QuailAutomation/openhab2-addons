/**
 * Copyright (c) 2014,2017 by the respective copyright holders.
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
package org.openhab.binding.marlinfireplace.internal;

import static org.openhab.binding.marlinfireplace.MarlinFireplaceBindingConstants.THING_TYPE_FIREPLACE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.marlinfireplace.handler.MarlinFireplaceHandler;
import org.openhab.io.transport.mqtt.MqttService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The {@link MarlinFireplaceHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.marlinfireplace")
public class MarlinFireplaceHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_FIREPLACE);

    @SuppressWarnings({ "null", "unused" })
    private MarlinFireplaceHandler handler = null;
    private MqttService mqtt = null;

    public MarlinFireplaceHandlerFactory() {
        super();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_FIREPLACE)) {
            this.handler = new MarlinFireplaceHandler(thing);
            this.handler.setMqttService(this.mqtt);
            return this.handler;
        }

        return null;
    }

    /**
     * Set MQTT Service from DS.
     *
     * @param mqttService
     *            to set.
     */
    // OSGI declarative services def for mqtt
    @Reference(name = "MQTTService", service = MqttService.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "unsetMqttService")
    public void setMqttService(MqttService mqttService) {
        this.mqtt = mqttService;
        if (this.handler != null) {
            this.handler.setMqttService(mqttService);
        }
    }

    /**
     * Unset MQTT Service from DS.
     *
     * @param mqttService
     *            to remove.
     */
    public void unsetMqttService(MqttService mqttService) {
        this.mqtt = null;
        if (this.handler != null) {
            this.handler.unsetMqttService(this.mqtt);
        }
    }
}
