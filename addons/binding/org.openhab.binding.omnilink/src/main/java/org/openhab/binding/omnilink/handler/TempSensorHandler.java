package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.Message;
import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.ObjectStatus;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.AuxSensorStatus;

/**
 *
 * @author Craig Hamilton
 *
 */
public class TempSensorHandler extends AbstractOmnilinkHandler<AuxSensorStatus> implements ThingHandler {

    private Logger logger = LoggerFactory.getLogger(TempSensorHandler.class);

    public TempSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void updateChannels(AuxSensorStatus status) {
        logger.debug("Aux Sensor Status {}", status);

        TemperatureFormat temperatureFormat = getOmnilinkBridgeHander().getTemperatureFormat();

        updateState(OmnilinkBindingConstants.CHANNEL_AUX_TEMP,
                new DecimalType(temperatureFormat.omniToFormat(status.getTemp())));
        updateState(OmnilinkBindingConstants.CHANNEL_AUX_LOW_SETPOINT,
                new DecimalType(temperatureFormat.omniToFormat(status.getCoolSetpoint())));
        updateState(OmnilinkBindingConstants.CHANNEL_AUX_HIGH_SETPOINT,
                new DecimalType(temperatureFormat.omniToFormat(status.getHeatSetpoint())));
    }

    @Override
    protected Optional<AuxSensorStatus> retrieveStatus() {
        try {
            int sensorID = getThingNumber();
            ObjectStatus objStatus = getOmnilinkBridgeHander().requestObjectStatus(Message.OBJ_TYPE_AUX_SENSOR,
                    sensorID, sensorID, true);
            return Optional.of((AuxSensorStatus) objStatus.getStatuses()[0]);
        } catch (OmniInvalidResponseException | OmniUnknownMessageTypeException | BridgeOfflineException e) {
            logger.debug("Unexpected exception refreshing unit:", e);
            return Optional.empty();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands for Aux Sensors
    }
}
