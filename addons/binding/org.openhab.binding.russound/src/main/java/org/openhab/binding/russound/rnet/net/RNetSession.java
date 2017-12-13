/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openhab.binding.russound.internal.net.SocketChannelSession;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a restartable socket connection to the underlying telnet session. Commands can be sent via
 * {@link #sendCommand(String)} and responses will be received on any {@link SocketSessionListener}. This implementation
 * of {@link SocketSession} communicates using a {@link SocketChannel} connection.
 *
 * @author Tim Roberts
 */
public class RNetSession<T> {
    private final Logger logger = LoggerFactory.getLogger(SocketChannelSession.class);

    /**
     * The host/ip address to connect to
     */
    private final String host;

    /**
     * The port to connect to
     */
    private final int port;

    /**
     * The actual socket being used. Will be null if not connected
     */
    private final AtomicReference<SocketChannel> socketChannel = new AtomicReference<SocketChannel>();

    /**
     * The responses read from the {@link #responseReader}
     */
    private final BlockingQueue<Object> responses = new ArrayBlockingQueue<Object>(50);
    private ResponseReader responseReader;
    /**
     * The {@link SocketSessionListener} that the {@link #dispatcher} will call
     */
    private List<SessionListener<T>> sessionListeners = new CopyOnWriteArrayList<SessionListener<T>>();

    /**
     * The thread dispatching responses - will be null if not connected
     */
    private Thread dispatchingThread = null;

    /**
     * The thread processing responses - will be null if not connected
     */
    private Thread responseThread = null;

    /**
     * Creates the socket session from the given host and port
     *
     * @param host a non-null, non-empty host/ip address
     * @param port the port number between 1 and 65535
     */
    public RNetSession(String host, int port, ResponseReader responseReader) {
        if (host == null || host.trim().length() == 0) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.host = host;
        this.port = port;

        this.responseReader = responseReader;

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.russound.internal.net.SocketSession#addListener(org.openhab.binding.russound.internal.net.
     * SocketSessionListener)
     */
    public void addListener(SessionListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        sessionListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#clearListeners()
     */
    public void clearListeners() {
        sessionListeners.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.russound.internal.net.SocketSession#removeListener(org.openhab.binding.russound.internal.net.
     * SocketSessionListener)
     */
    public boolean removeListener(SessionListener<T> listener) {
        return sessionListeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#connect()
     */
    public void connect() throws IOException {
        connect(2000);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#connect(int)
     */
    public void connect(int timeout) throws IOException {
        disconnect();

        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(true);
        responseReader.setQueueandChannel(responses, socketChannel);

        logger.debug("Connecting to {}:{}", host, port);
        channel.socket().connect(new InetSocketAddress(host, port), timeout);

        socketChannel.set(channel);

        responses.clear();

        dispatchingThread = new Thread(new Dispatcher());
        responseThread = new Thread(responseReader);

        dispatchingThread.start();
        responseThread.start();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#disconnect()
     */
    public void disconnect() throws IOException {
        if (isConnected()) {
            logger.debug("Disconnecting from {}:{}", host, port);

            final SocketChannel channel = socketChannel.getAndSet(null);
            channel.close();

            dispatchingThread.interrupt();
            dispatchingThread = null;

            responseThread.interrupt();
            responseThread = null;

            responses.clear();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#isConnected()
     */
    public boolean isConnected() {
        final SocketChannel channel = socketChannel.get();
        return channel != null && channel.isConnected();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.russound.internal.net.SocketSession#sendCommand(java.lang.String)
     */
    public synchronized void sendCommand(T command) throws IOException {
        sendCommand(command.toString().getBytes());
    }

    public synchronized void sendCommand(byte[] command) throws IOException {
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }

        if (!isConnected()) {
            throw new IOException("Cannot send message - disconnected");
        }

        final SocketChannel channel = socketChannel.get();
        if (channel == null) {
            logger.debug("Cannot send command '{}' - socket channel was closed", command);
        } else {
            logger.debug("Sending Command: '{}'", command);
            channel.write(ByteBuffer.wrap(command));
        }
    }

    /**
     * The dispatcher runnable is responsible for reading the response queue and dispatching it to the current callable.
     * Since the dispatcher is ONLY started when a callable is set, responses may pile up in the queue and be dispatched
     * when a callable is set. Unlike the socket reader, this can be assigned to another thread (no state outside of the
     * class).
     *
     * @author Tim Roberts
     */
    private class Dispatcher implements Runnable {
        /**
         * Runs the logic to dispatch any responses to the current listeners until {@link #isRunning} is false.
         */
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final SessionListener<T>[] listeners = sessionListeners.toArray(new SessionListener[0]);

                    // if no listeners, we don't want to start dispatching yet.
                    if (listeners.length == 0) {
                        Thread.sleep(250);
                        continue;
                    }

                    final Object response = responses.poll(1, TimeUnit.SECONDS);

                    if (response != null) {
                        if (response instanceof IOException) {
                            logger.debug("Dispatching exception: {}", response);
                            for (SessionListener<T> listener : listeners) {
                                listener.responseException((IOException) response);
                            }
                        } else {
                            logger.trace("Dispatching response: {}", response);
                            for (SessionListener<T> listener : listeners) {
                                listener.responseReceived((T) response);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    // Ending thread execution
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.debug("Uncaught exception {}: {}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
