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

    public void onDeviceOffLine();

    public void onDeviceOnLine();

    public void onNodeAdded(Event event);

    public void onNodeChanged(Event event);

    public void onNodeRemoved(Event event);

    public void onNodeRenamed(Event event);

    public void onSceneAdded(Event event);

    public void onSceneLinkAdded(Event event);

    public void onSceneLinkRemoved(Event event);

    public void onSceneRemoved(Event event);

    public void onSceneRenamed(Event event);

    public void onVariableChanged(VariableEvent event);

}
