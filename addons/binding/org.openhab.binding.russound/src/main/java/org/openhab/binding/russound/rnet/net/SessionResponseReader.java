/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.rnet.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This is the runnable that will read from the socket and add messages to the responses queue (to be processed by
 * the dispatcher)
 *
 * @author Tim Roberts
 *
 */
public class SessionResponseReader<T> implements Runnable {

    /**
     * The actual socket being used. Will be null if not connected
     */
    private final AtomicReference<SocketChannel> socketChannel = new AtomicReference<SocketChannel>();

    /**
     * The responses read from the {@link #responseReader}
     */
    private final BlockingQueue<Object> responses = new ArrayBlockingQueue<Object>(50);

    /**
     * Runs the logic to read from the socket until {@link #isRunning} is false. A 'response' is anything that ends
     * with a carriage-return/newline combo. Additionally, the special "Login: " and "Password: " prompts are
     * treated as responses for purposes of logging in.
     */
    @Override
    public void run() {
        final StringBuilder sb = new StringBuilder(100);
        final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        responses.clear();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // if reader is null, sleep and try again
                if (readBuffer == null) {
                    Thread.sleep(250);
                    continue;
                }

                final SocketChannel channel = socketChannel.get();
                if (channel == null) {
                    // socket was closed
                    Thread.currentThread().interrupt();
                    break;
                }

                int bytesRead = channel.read(readBuffer);
                if (bytesRead == -1) {
                    responses.put(new IOException("server closed connection"));
                    break;
                } else if (bytesRead == 0) {
                    readBuffer.clear();
                    continue;
                }

                readBuffer.flip();
                while (readBuffer.hasRemaining()) {
                    final char ch = (char) readBuffer.get();
                    sb.append(ch);
                    if (ch == '\n' || ch == ' ') {
                        final String str = sb.toString();
                        if (str.endsWith("\r\n") || str.endsWith("Login: ") || str.endsWith("Password: ")) {
                            sb.setLength(0);
                            final String response = str.substring(0, str.length() - 2);
                            responses.put(response);
                        }
                    }
                }

                readBuffer.flip();

            } catch (InterruptedException e) {
                // Ending thread execution
                Thread.currentThread().interrupt();
            } catch (AsynchronousCloseException e) {
                // socket was closed by another thread but interrupt our loop anyway
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                // set before pushing the response since we'll likely call back our stop
                Thread.currentThread().interrupt();

                try {
                    responses.put(e);
                    break;
                } catch (InterruptedException e1) {
                    // Do nothing - probably shutting down
                    // Since we set isRunning to false, will drop out of loop and end the thread
                }
            }
        }
    }
}
