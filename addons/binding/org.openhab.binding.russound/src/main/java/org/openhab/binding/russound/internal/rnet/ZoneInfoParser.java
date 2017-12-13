/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detect the generic zone info messages on RNet bus and update the various channels
 *
 * @author Craig Hamilton
 *
 */
public class ZoneInfoParser implements BusParser {
    private final Logger logger = LoggerFactory.getLogger(ZoneInfoParser.class);

    @Override
    public boolean matches(Byte[] bytes) {
        boolean matches = bytes[0] == (byte) 0xF0 && bytes[3] == (byte) 0x70 && bytes[9] == (byte) 0x04
                && bytes[10] == (byte) 0x02;
        if (matches) {
            logger.debug("Found zone info message: {}", StringHexUtils.byteArrayToHex(bytes));
        }
        return matches;
    }

    @Override
    public ZoneStateUpdate process(Byte[] bytes) {
        ZoneId zoneId = new ZoneId(bytes[4] + 1, bytes[12] + 1);
        List<ChannelStateUpdate> actions = new ArrayList<>();

        actions.add(new ChannelStateUpdate(RNetConstants.CHANNEL_ZONESTATUS,
                bytes[20] == 1 ? OnOffType.ON : OnOffType.OFF));
        actions.add(new ChannelStateUpdate(RNetConstants.CHANNEL_ZONEVOLUME, new PercentType(bytes[22] * 2)));
        actions.add(new ChannelStateUpdate(RNetConstants.CHANNEL_ZONESOURCE, new DecimalType(bytes[21] + 1)));
        actions.add(new ChannelStateUpdate(RNetConstants.CHANNEL_ZONEBALANCE, new DecimalType(bytes[26] - 10)));
        actions.add(new ChannelStateUpdate(RNetConstants.CHANNEL_ZONETREBLE, new DecimalType(bytes[24] - 10)));
        actions.add(new ChannelStateUpdate(RNetConstants.CHANNEL_ZONELOUDNESS,
                bytes[25] == 1 ? OnOffType.ON : OnOffType.OFF));
        actions.add(new ChannelStateUpdate(RNetConstants.CHANNEL_ZONEBASS, new DecimalType(bytes[23] - 10)));
        return new ZoneStateUpdate(zoneId, actions);
    }

}
