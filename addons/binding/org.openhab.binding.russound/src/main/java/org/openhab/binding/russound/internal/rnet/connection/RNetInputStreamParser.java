/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.openhab.binding.russound.internal.rnet.StringHexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for watching for valid RNet messages and then passing them to the binding
 *
 * @author Craig Hamilton
 *
 */
public class RNetInputStreamParser implements InputParser {
    private Logger logger = LoggerFactory.getLogger(RNetInputStreamParser.class);
    byte[] partialBytes = new byte[1000];

    private InputHander inputHandler;

    public RNetInputStreamParser(InputHander handler) {
        this.inputHandler = handler;
    }

    @Override
    public void parse(byte[] bytes) {
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
                this.inputHandler.handle(ArrayUtils.toObject(someBytes));
            }
        }

    }

    private byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}
