package org.openhab.binding.isy.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.isy.IsyBindingConstants;
import org.openhab.binding.isy.config.IsyVariableConfiguration;
import org.openhab.binding.isy.internal.OHIsyClient;
import org.openhab.binding.isy.internal.VariableType;
import org.openhab.binding.isy.internal.protocol.VariableEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsyVariableHandler extends AbtractIsyThingHandler {
    private static final Logger logger = LoggerFactory.getLogger(IsyVariableHandler.class);

    public IsyVariableHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            IsyVariableConfiguration var_config = getThing().getConfiguration().as(IsyVariableConfiguration.class);
            logger.trace("Variable config: {}", var_config);
            OHIsyClient insteonClient = getBridgeHandler().getInsteonClient();
            if (insteonClient != null) {
                VariableEvent currentValue = insteonClient.getVariableValue(VariableType.fromInt(var_config.type),
                        var_config.id);
                logger.trace("CurrentValue: {}", currentValue);
                handleUpdate(currentValue.getVal());
            } else {
                logger.warn("Insteon client is null");
            }
        } else

        {
            if (command instanceof DecimalType) {
                IsyVariableConfiguration var_config = getThing().getConfiguration().as(IsyVariableConfiguration.class);
                getBridgeHandler().getInsteonClient().changeVariableState(VariableType.fromInt(var_config.type),
                        var_config.id, ((DecimalType) command).intValue());
            } else {
                logger.warn("Unsupported command for variable handleCommand: " + command.toFullString());
            }
        }

    }

    public void handleUpdate(int value) {
        updateState(IsyBindingConstants.CHANNEL_VARIABLE_VALUE, new DecimalType(value));
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
