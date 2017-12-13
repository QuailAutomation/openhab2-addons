/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet;

import org.eclipse.smarthome.core.types.State;

/**
 * Represents a desired change for channel
 *
 * @author Craig Hamilton
 *
 */
public class ChannelStateUpdate {

    private String channel;
    private State state;

    public String getChannel() {
        return channel;
    }

    public State getState() {
        return state;
    }

    public ChannelStateUpdate(String channel, State state) {
        super();
        this.channel = channel;
        this.state = state;
    }

}
