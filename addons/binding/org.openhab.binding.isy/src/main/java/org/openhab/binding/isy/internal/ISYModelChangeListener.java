/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.isy.internal;

import org.openhab.binding.isy.internal.protocol.Event;
import org.openhab.binding.isy.internal.protocol.VariableEvent;

/**
 * @author Craig Hamilton
 *
 */
public interface ISYModelChangeListener {
    public void onModelChanged(Event event);

    public void onVariableChanged(VariableEvent event);

    public void onDeviceOnLine();

    public void onDeviceOffLine();

    public void stillAlive();

}
