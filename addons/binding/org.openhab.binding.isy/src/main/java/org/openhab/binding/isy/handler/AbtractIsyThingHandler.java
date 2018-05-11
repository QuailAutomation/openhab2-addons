package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

public abstract class AbtractIsyThingHandler extends BaseThingHandler {

    protected AbtractIsyThingHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    protected IsyBridgeHandler getBridgeHandler() {
        return (IsyBridgeHandler) getBridge().getHandler();
    }

}
