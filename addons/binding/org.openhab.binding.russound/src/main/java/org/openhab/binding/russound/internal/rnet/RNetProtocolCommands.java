/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rnet;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide mapping between ZoneCommands and their RNet byte representation
 *
 * @author Craig Hamilton
 *
 */
public class RNetProtocolCommands implements RNetCommand {
    private static Logger logger = LoggerFactory.getLogger(RNetProtocolCommands.class);
    private static final int NO_VALUE_NEEDED = -33;
    public static final byte NO_VALUE = (byte) 0xFF;

    /**
     * Currently supported Rnet Commands
     *
     * @author Craig Hamilton
     *
     */
    public enum ZoneCommand {
        VOLUME_SET,
        POWER_SET,
        SOURCE_SET,
        BASS_SET,
        ZONE_INFO,
        BALANCE_SET,
        LOUDNESS_SET,
        TREBLE_SET,
        TURNONVOLUME_SET,
        MUTE,
        ALLONOFF_SET
    }

    private static Byte[] volumeBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x05, (byte) 0x02, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0xf1,
            (byte) 0x21, (byte) 0x00, (byte) 0x12, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };
    private static Byte[] powerBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x05, (byte) 0x02, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0xf1,
            (byte) 0x23, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x01 };
    private static Byte[] sourceBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xf1,
            (byte) 0x3e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };
    private static Byte[] bassBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x00, (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x01 };
    private static Byte[] zoneInfoBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x01, (byte) 0x04, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x07,
            (byte) 0x00, (byte) 0x00 };
    private static Byte[] balanceBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x00, (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00 };
    private static Byte[] loudnessBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x00, (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00 };
    private static Byte[] trebleBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x00, (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00 };
    private static Byte[] turnOnVolumeBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f,
            (byte) 0x00, (byte) 0x00, (byte) 0x70, (byte) 0x00, (byte) 0x05, (byte) 0x02, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01,
            (byte) 0x00, (byte) 0x00 };
    private static Byte[] muteBytes = new Byte[] { (byte) 0xf0, (byte) 0x00, (byte) 0x00, (byte) 0x7f, (byte) 0x00,
            (byte) 0x00, (byte) 0x70, (byte) 0x05, (byte) 0x02, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0xf1,
            (byte) 0x40, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D, (byte) 0x00, (byte) 0x01 };

    private static RNetCommand[] zoneCommands = {
            new RNetProtocolCommands(volumeBytes, new int[] { 1, 4 }, new int[] { 5, 17 }, 15),
            new RNetProtocolCommands(powerBytes, new int[] { 1, 4 }, new int[] { 5, 17 }, 15),
            new RNetProtocolCommands(sourceBytes, new int[] { 1, 4 }, 5, 17),
            new RNetProtocolCommands(bassBytes, new int[] { 1, 4 }, new int[] { 5, 11 }, 21),
            new RNetProtocolCommands(zoneInfoBytes, new int[] { 1 }, new int[] { 11 }, 2),
            new RNetProtocolCommands(balanceBytes, new int[] { 1, 4 }, new int[] { 5, 11 }, 21),
            new RNetProtocolCommands(loudnessBytes, new int[] { 1, 4 }, new int[] { 5, 11 }, 21),
            new RNetProtocolCommands(trebleBytes, new int[] { 1, 4 }, new int[] { 5, 11 }, 21),
            new RNetProtocolCommands(turnOnVolumeBytes, new int[] { 1, 4 }, new int[] { 5, 11 }, 21),
            new RNetProtocolCommands(muteBytes, new int[] { 4 }, new int[] { 5 }, NO_VALUE_NEEDED),
            new RNetAllOnOffCommand() };

    private Byte[] commandBytes;
    private int[] zoneBytes;
    private int[] controllerBytes;
    private int valueOrdinal;

    private RNetProtocolCommands(Byte[] commandBytes, int[] controllerByteOrdinals, int[] zoneByteOrdinals,
            int valueByteOrdinal) {
        this.commandBytes = commandBytes;
        this.zoneBytes = zoneByteOrdinals;
        this.controllerBytes = controllerByteOrdinals;
        this.valueOrdinal = valueByteOrdinal;
    }

    private RNetProtocolCommands(Byte[] commandBytes, int controllerBytes, int zoneBytes, int valueByte) {
        this(commandBytes, new int[] { zoneBytes }, new int[] { controllerBytes }, valueByte);
    }

    private RNetProtocolCommands(Byte[] commandBytes, int[] controllerBytes, int zoneBytes, int valueByte) {
        this(commandBytes, controllerBytes, new int[] { zoneBytes }, valueByte);

    }

    private RNetProtocolCommands(Byte[] commandBytes, int controllerBytes, int[] zoneBytes, int valueByte) {
        this(commandBytes, new int[] { controllerBytes }, zoneBytes, valueByte);
    }

    @Override
    public Byte[] getCommand(ZoneId zoneId, byte value) {
        logger.debug("original command message: {}", StringHexUtils.byteArrayToHex(this.commandBytes));
        Byte[] commandByteCopy = Arrays.copyOf(this.commandBytes, this.commandBytes.length);
        logger.debug("after copy command message: {}", StringHexUtils.byteArrayToHex(commandByteCopy));
        for (Integer zoneOrdinal : zoneBytes) {
            commandByteCopy[zoneOrdinal] = (byte) (zoneId.getZoneId() - 1);
        }
        for (Integer controllerOrdinal : controllerBytes) {
            commandByteCopy[controllerOrdinal] = (byte) (zoneId.getControllerId() - 1);
        }

        if (this.valueOrdinal != NO_VALUE_NEEDED) {
            commandByteCopy[this.valueOrdinal] = value;
        }

        logger.debug("getCommandReturning zone: {}, controller: {}, value: {}", zoneId.getZoneId(),
                zoneId.getControllerId(), value);
        logger.debug("Bytes: {}", StringHexUtils.byteArrayToHex(commandByteCopy));
        return commandByteCopy;
    }

    public static Byte[] getCommand(ZoneCommand command, ZoneId zoneId, byte value) {
        logger.debug("getCommand called, command: {}, zoneId: {}, value: {}", command, zoneId, value);
        return zoneCommands[command.ordinal()].getCommand(zoneId, value);
    }
}