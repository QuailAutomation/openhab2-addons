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
 * config info for rnet system
 *
 * @author Craig Hamilton
 *
 */
public class RNetSystemConfig {

    private String connectionString;
    private int retryPolling;

    private int numControllers;
    private int zonesPer;

    public int getNumControllers() {
        return numControllers;
    }

    public void setNumControllers(int numControllers) {
        this.numControllers = numControllers;
    }

    public int getZonesPer() {
        return zonesPer;
    }

    public void setZonesPer(int zonesPer) {
        this.zonesPer = zonesPer;
    }

    public int getRetryPolling() {
        return retryPolling;
    }

    public void setRetryPolling(int retryPolling) {
        this.retryPolling = retryPolling;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String ipAddress) {
        this.connectionString = ipAddress;
    }

}
