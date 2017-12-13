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
 * Config info for Rnet Audio Zone
 * 
 * @author Craig Hamilton
 *
 */
public class RnetZoneConfig {
    private int controller;
    private int zone;

    public void setController(int controller) {
        this.controller = controller;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public int getController() {
        return controller;
    }

    public int getZone() {
        return zone;
    }

}
