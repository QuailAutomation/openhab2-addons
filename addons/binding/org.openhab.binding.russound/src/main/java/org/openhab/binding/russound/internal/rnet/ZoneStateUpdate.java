/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.smarthome.core.types.State;

/**
 * Represents a collection of attribute changes for a zone.
 *
 * @author Craig Hamilton
 *
 */
public class ZoneStateUpdate {
    private ZoneId zoneId;

    private Collection<ChannelStateUpdate> stateUpdates;

    public ZoneId getZoneId() {
        return zoneId;
    }

    public ZoneStateUpdate(ZoneId zoneId, String channel, State state) {
        this.zoneId = zoneId;
        this.stateUpdates = Arrays.asList(new ChannelStateUpdate(channel, state));
    }

    public ZoneStateUpdate(ZoneId zoneId, Collection<ChannelStateUpdate> stateUpdates) {
        this.zoneId = zoneId;
        this.stateUpdates = stateUpdates;
    }

    public Collection<ChannelStateUpdate> getStateUpdates() {
        return stateUpdates;
    }

}
