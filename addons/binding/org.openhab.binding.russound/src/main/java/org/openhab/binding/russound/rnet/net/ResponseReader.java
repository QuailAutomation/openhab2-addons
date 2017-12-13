/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.rnet.net;

import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Interface representing something which can read responses for rnet system
 * 
 * @author Craig Hamilton
 *
 */
public interface ResponseReader extends Runnable {

    void setQueueandChannel(BlockingQueue<Object> responses, AtomicReference<SocketChannel> socketChannel);

}
