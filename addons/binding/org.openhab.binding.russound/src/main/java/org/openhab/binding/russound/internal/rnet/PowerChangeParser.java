/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects if there has been a change to power for audio zone
 *
 * @author Craig Hamilton
 *
 */
public class PowerChangeParser implements BusParser {
    private final Logger logger = LoggerFactory.getLogger(PowerChangeParser.class);

    @Override
    public boolean matches(Byte[] bytes) {
        return (bytes[0] == (byte) 0xF0 && bytes[6] == (byte) 0x7f && bytes[7] == (byte) 0x05
                && bytes[14] == (byte) 0xf1 && bytes[15] == (byte) 0x23);

    }

    @Override
    public ZoneStateUpdate process(Byte[] bytes) {
        if (matches(bytes)) {
            logger.debug("Status change (power) detected, controller: {}, zone: {}, value={}", bytes[1] + 1,
                    bytes[19] + 1, bytes[17]);
            return new ZoneStateUpdate(new ZoneId(bytes[1] + 1, bytes[19] + 1), RNetConstants.CHANNEL_ZONESTATUS,
                    bytes[17] == 1 ? OnOffType.ON : OnOffType.OFF);
        } else {
            return null;
        }
    }

}
