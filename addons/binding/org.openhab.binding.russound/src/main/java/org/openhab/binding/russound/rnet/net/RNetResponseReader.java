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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.ArrayUtils;
import org.openhab.binding.russound.rnet.internal.StringHexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the runnable that will read from the socket and add messages to the responses queue (to be processed by
 * the dispatcher)
 *
 * @author Tim Roberts
 *
 */
public class RNetResponseReader implements ResponseReader {
    private Logger logger = LoggerFactory.getLogger(RNetResponseReader.class);
    /**
     * Whether the reader is currently running
     */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Locking to allow proper shutdown of the reader
     */
    private final CountDownLatch running = new CountDownLatch(1);

    private BlockingQueue<Object> responses = new ArrayBlockingQueue<Object>(50);

    /**
     * The actual socket being used. Will be null if not connected
     */
    private AtomicReference<SocketChannel> socketChannel = new AtomicReference<SocketChannel>();

    public RNetResponseReader() {
    }

    private byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    /**
     * Runs the logic to read from the socket until {@link #isRunning} is false. A 'response' is anything that ends
     * with a carriage-return/newline combo. Additionally, the special "Login: " and "Password: " prompts are
     * treated as responses for purposes of logging in.
     */
    @Override
    public void run() {
        final byte[] sb = new byte[1024];
        final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        isRunning.set(true);
        responses.clear();
        // int byteIndex = 0;
        byte[] partialBytes = null;
        while (isRunning.get()) {
            try {
                // if reader is null, sleep and try again
                if (readBuffer == null) {
                    Thread.sleep(250);
                    continue;
                }

                final SocketChannel channel = socketChannel.get();
                if (channel == null) {
                    // socket was closed
                    isRunning.set(false);
                    break;
                }

                int bytesRead = channel.read(readBuffer);

                // _logger.debug("Bytes read: {}", StringHexUtils.byteArrayToHex(readData));

                if (bytesRead == -1) {
                    responses.put(new IOException("server closed connection"));
                    isRunning.set(false);
                    break;
                } else if (bytesRead == 0) {
                    readBuffer.clear();
                    continue;
                }
                readBuffer.flip();
                while (readBuffer.hasRemaining()) {
                    byte[] bytes = new byte[bytesRead];
                    readBuffer.get(bytes);
                    byte[] trimmedData;
                    // should add partial bytes from previous if we have them
                    if (partialBytes != null && partialBytes.length > 0) {
                        trimmedData = concat(partialBytes, bytes);
                    } else {
                        trimmedData = bytes;
                    }

                    List<byte[]> byteArrays = new ArrayList<byte[]>();
                    int lastStart = 0;
                    for (int i = 0; i < trimmedData.length; i++) {
                        if (trimmedData[i] == (byte) 0xf7) {
                            // logger.debug("found terminator at index: " + i);

                            byte[] aNewArray = Arrays.copyOfRange(trimmedData, lastStart, i + 1);
                            byteArrays.add(aNewArray);
                            lastStart = i + 1;
                        }
                    }
                    logger.trace("last start: {}", lastStart);
                    partialBytes = Arrays.copyOfRange(trimmedData, lastStart, trimmedData.length);
                    logger.trace("partial bytes: {}", StringHexUtils.byteArrayToHex(partialBytes));
                    for (byte[] someBytes : byteArrays) {
                        int bytesLen = someBytes.length;
                        logger.trace("elements in byte array: {}", bytesLen);
                        if (someBytes[bytesLen - 1] == (byte) 0xf7) { // end-flag
                            logger.debug("Russound message: {}", StringHexUtils.byteArrayToHex(someBytes));
                            responses.put(ArrayUtils.toObject(someBytes));
                        }
                    }

                }
                readBuffer.flip();
                // _logger.debug("readBuffer position: {}", readBuffer.position());
            } catch (InterruptedException e) {
                // Do nothing - probably shutting down
            } catch (AsynchronousCloseException e) {
                // socket was definitely closed by another thread
            } catch (IOException e) {
                try {
                    isRunning.set(false);
                    responses.put(e);
                } catch (InterruptedException e1) {
                    // Do nothing - probably shutting down
                }
            }
        }

        running.countDown();
    }

    @Override
    public void setQueueandChannel(BlockingQueue<Object> responseQueue, AtomicReference<SocketChannel> socketChannel) {
        responses = responseQueue;
        this.socketChannel = socketChannel;

    }
}