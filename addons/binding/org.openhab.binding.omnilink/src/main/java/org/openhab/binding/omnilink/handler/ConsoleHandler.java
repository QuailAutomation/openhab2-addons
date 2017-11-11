package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.OmniInvalidResponseException;
import com.digitaldan.jomnilinkII.OmniUnknownMessageTypeException;
import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;

/**
 *
 * @author Craig Hamilton
 *
 */
public class ConsoleHandler extends AbstractOmnilinkHandler {
    private Logger logger = LoggerFactory.getLogger(ConsoleHandler.class);

    public ConsoleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handling command for console.  channel: {}, command: {}", channelUID, command);
        int cmd;
        int p1;
        if (OmnilinkBindingConstants.CHANNEL_CONSOLE_ENABLE_BEEPER.equals(channelUID.getId())
                && command instanceof OnOffType) {
            cmd = CommandMessage.CMD_CONSOLE_ENABLE_DISABLE_BEEPER;
            p1 = command.equals(OnOffType.OFF) ? 0 : 1;
        } else if (OmnilinkBindingConstants.CHANNEL_CONSOLE_BEEP.equals(channelUID.getId())
                && command instanceof DecimalType) {
            cmd = CommandMessage.CMD_CONSOLE_BEEP;
            p1 = ((DecimalType) command).intValue();
        } else {
            logger.error("Unknown channel {} with command {}", channelUID.getAsString(), command);
            return;
        }

        int consoleNumber = getThingNumber();
        try {
            getOmnilinkBridgeHander().sendOmnilinkCommand(cmd, p1, consoleNumber);
        } catch (NumberFormatException | OmniInvalidResponseException | OmniUnknownMessageTypeException
                | BridgeOfflineException e) {
            logger.debug("Could not send console command to omnilink", e);
        }

    }

    @Override
    protected Optional retrieveStatus() {
        return Optional.empty();
    }

    @Override
    protected void updateChannels(Status t) {
        // No Status.
    }
}
