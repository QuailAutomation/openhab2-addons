/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.marlinhottub.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.marlinhottub.MarlinHotTubBindingConstants;
import org.openhab.binding.marlinhottub.handler.MarlinHotTubHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MarlinHotTubHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.marlinhottub")
@NonNullByDefault
public class MarlinHotTubHandlerFactory extends BaseThingHandlerFactory {

    @SuppressWarnings("null")
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(MarlinHotTubBindingConstants.THING_TYPE_MARLINHOTTUB);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MarlinHotTubBindingConstants.THING_TYPE_MARLINHOTTUB)) {
            return new MarlinHotTubHandler(thing);
        }

        return null;
    }
}
