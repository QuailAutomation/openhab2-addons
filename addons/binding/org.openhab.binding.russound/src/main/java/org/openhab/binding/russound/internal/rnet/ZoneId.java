/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet;

/**
 * Represents an audio zone id
 *
 * @author Craig Hamilton
 *
 */
public class ZoneId {

    private int controllerId;
    private int zoneId;

    // just so we don't have to create this every time...
    private String toStringString;

    public ZoneId(int controller, int zone) {
        this.controllerId = controller;
        this.zoneId = zone;
        toStringString = "[ZoneId]controller: " + this.controllerId + ", zoneId: " + this.zoneId;
    }

    public int getControllerId() {
        return controllerId;
    }

    public int getZoneId() {
        return zoneId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZoneId other = (ZoneId) obj;
        return this.controllerId == other.controllerId && this.zoneId == other.zoneId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.controllerId;
        result = prime * result + this.zoneId;
        return result;
    }

    @Override
    public String toString() {

        return toStringString;
    }

}
