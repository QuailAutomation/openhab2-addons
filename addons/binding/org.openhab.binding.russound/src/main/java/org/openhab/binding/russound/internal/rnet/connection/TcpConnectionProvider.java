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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This provides a tcp connection.
 *
 * @author Craig Hamilton
 *
 */
public class TcpConnectionProvider implements ConnectionProvider {

    private final Logger logger = LoggerFactory.getLogger(TcpConnectionProvider.class);

    /** Connection timeout in milliseconds **/
    private static final int CONNECTION_TIMEOUT = 5000;

    /** Instantiated class IP for the receiver to communicate with. **/
    private String receiverIP = "192.168.1.30";

    /** default port. **/
    public static final int DEFAULT_EISCP_PORT = 7777;

    /** Connection test interval in milliseconds **/
    private static final int CONNECTION_TEST_INTERVAL = 20000;
    /** Socket timeout in milliseconds **/
    private static final int SOCKET_TIMEOUT = CONNECTION_TEST_INTERVAL + 10000;

    /** Instantiated class Port for the receiver to communicate with. **/
    private int receiverPort = DEFAULT_EISCP_PORT;

    private Socket socket = null;
    private ObjectOutputStream outStream = null;
    private DataInputStream inStream = null;
    private boolean connected = false;

    public TcpConnectionProvider(String ip, int port) {
        if (StringUtils.isNotBlank(ip)) {
            receiverIP = ip;
        }
        if (port >= 1) {
            receiverPort = port;
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public ObjectOutputStream getOutputStream() {
        return outStream;
    }

    @Override
    public DataInputStream getInputStream() {
        return inStream;
    }

    /**
     * Connects to the receiver by opening a socket connection through the IP
     * and port.
     **/
    @Override
    public boolean connect() {

        if (socket == null || !connected || !socket.isConnected()) {
            try {
                // Creating a socket to connect to the server
                socket = new Socket();
                socket.connect(new InetSocketAddress(receiverIP, receiverPort), CONNECTION_TIMEOUT);

                logger.debug("Connected to {} on port {}", receiverIP, receiverPort);

                // Get Input and Output streams
                outStream = new ObjectOutputStream(socket.getOutputStream());
                inStream = new DataInputStream(socket.getInputStream());

                socket.setSoTimeout(SOCKET_TIMEOUT);
                outStream.flush();
                connected = true;

            } catch (UnknownHostException unknownHost) {
                logger.error("You are trying to connect to an unknown host!", unknownHost);
            } catch (IOException ioException) {
                logger.error("Can't connect: ", ioException.getMessage());
            }
        }

        return connected;
    }

    @Override
    public void disconnect() throws IOException {
        if (inStream != null) {
            inStream.close();
            inStream = null;
            logger.debug("closed input stream!");
        }
        if (outStream != null) {
            outStream.close();
            outStream = null;
            logger.debug("closed output stream!");
        }
        if (socket != null) {
            socket.close();
            socket = null;
            logger.debug("closed socket!");
        }

    }
}
