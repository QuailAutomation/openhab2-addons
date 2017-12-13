/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet.connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Represents something which can provide a connection with in/out streams
 *
 * @author Craig Hamilton
 *
 */
public interface ConnectionProvider {

    public ObjectOutputStream getOutputStream();

    public DataInputStream getInputStream();

    public boolean isConnected();

    public boolean connect() throws NoConnectionException;

    public void disconnect() throws IOException;
}
