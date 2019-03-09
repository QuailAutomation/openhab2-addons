/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.hueemulation.internal;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Network utility methods
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Utils {
    /**
     * Sets Hue API Headers
     */
    static void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }

    /**
     * Try to get the ethernet interface MAC for the network interface that belongs to the given IP address.
     * Returns a default MAC on any failure.
     *
     * @param address IP address
     * @return A MAC of the form "00:00:88:00:bb:ee"
     */
    static String getMAC(InetAddress address) {
        NetworkInterface networkInterface;
        final byte[] mac;
        try {
            networkInterface = NetworkInterface.getByInetAddress(address);
            if (networkInterface == null) {
                return "00:00:88:00:bb:ee";
            }
            mac = networkInterface.getHardwareAddress();
            if (mac == null) {
                return "00:00:88:00:bb:ee";
            }
        } catch (SocketException e) {
            return "00:00:88:00:bb:ee";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        return sb.toString();
    }

}
