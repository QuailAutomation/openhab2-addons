/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.hueemulation.internal.dto;

/**
 * Hue API state object for plugs
 *
 * @author David Graeff - Initial contribution
 *
 */
public class HueStatePlug extends AbstractHueState {
    public boolean on;

    protected HueStatePlug() {
    }

    public HueStatePlug(boolean on) {
        this.on = on;
    }

    @Override
    public String toString() {
        return "on: " + on + ", reachable: " + reachable;
    }
}
