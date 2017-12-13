/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet;

import org.apache.commons.lang.ArrayUtils;

/**
 * Utility class for converting bytes to hex string
 *
 * @author Craig Hamilton
 *
 */
public class StringHexUtils {

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02x", b & 0xff));
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String byteArrayToHex(Byte[] a) {
        return byteArrayToHex(ArrayUtils.toPrimitive(a));
    }
}