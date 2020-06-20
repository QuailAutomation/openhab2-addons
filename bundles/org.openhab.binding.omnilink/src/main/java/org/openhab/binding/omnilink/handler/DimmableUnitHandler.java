/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.omnilink.handler;

import static org.openhab.binding.omnilink.OmnilinkBindingConstants.CHANNEL_UNIT_LEVEL;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

/**
 *
 * @author Brian O'Connell
 *
 */
public class DimmableUnitHandler extends UnitHandler {

    public DimmableUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_UNIT_LEVEL:
                handleUnitLevel(channelUID, command);
                break;
            default:
                super.handleCommand(channelUID, command);
        }
    }

    private void handleUnitLevel(@NonNull ChannelUID channelUID, @NonNull Command command) {

        final int unitId = getThingNumber();

        if (command instanceof PercentType) {
            handlePercent(command, unitId);

        } else if (command instanceof IncreaseDecreaseType) {
            handleIncreaseDecrease(command, unitId);

        } else {
            // Only handle percent or increase/decrease.
            super.handleCommand(channelUID, command);
        }
    }

    private void handleIncreaseDecrease(Command command, final int unitId) {
        final OmniLinkCmd omniCmd = command == IncreaseDecreaseType.INCREASE ? OmniLinkCmd.CMD_UNIT_UNIT_BRIGHTEN_STEP_1
                : OmniLinkCmd.CMD_UNIT_UNIT_DIM_STEP_1;
        sendOmnilinkCommand(omniCmd.getNumber(), 0, unitId);
    }

    private void handlePercent(Command command, final int unitId) {
        int lightLevel = ((PercentType) command).intValue();

        if (lightLevel == 0) {
            super.handleOnOff(OnOffType.OFF, unitId);
        } else if (lightLevel == 100) {
            super.handleOnOff(OnOffType.ON, unitId);
        } else {
            sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_PERCENT.getNumber(), lightLevel, unitId);
        }
    }

}