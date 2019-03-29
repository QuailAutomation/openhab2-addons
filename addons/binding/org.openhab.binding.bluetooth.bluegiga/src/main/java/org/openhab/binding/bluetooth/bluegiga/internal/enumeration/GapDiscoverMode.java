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
package org.openhab.binding.bluetooth.bluegiga.internal.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to implement the BlueGiga Enumeration <b>GapDiscoverMode</b>.
 * <p>
 * GAP discover modes
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
public enum GapDiscoverMode {
    /**
     * Default unknown value
     */
    UNKNOWN(-1),

    /**
     * [0] Discover only limited discoverable devices, that is, Slaves which have the LE Limited
     * Discoverable Mode bit set in the AD type of their Flags advertisement packets.
     */
    GAP_DISCOVER_LIMITED(0x0000),

    /**
     * [1] Discover limited and generic discoverable devices, that is, Slaves which have the LE
     * Limited Discoverable Mode LE General or the Discoverable Mode bit set in the AD type of their
     * advertisement Flags packets.
     */
    GAP_DISCOVER_GENERIC(0x0001),

    /**
     * [2] Discover all devices regardless of the AD type, so also devices in Flags
     * non-discoverable mode will be reported to host.
     */
    GAP_DISCOVER_OBSERVATION(0x0002),

    /**
     * [3] Same as gap_non_discoverable.
     */
    GAP_BROADCAST(0x0003),

    /**
     * [4] In this advertisement the advertisement and scan response data defined by user will be
     * used. The user is responsible of building the advertisement data so that it also contains the
     * appropriate desired Flags AD type.
     */
    GAP_USER_DATA(0x0004),

    /**
     * [128] When turning the most highest bit on in GAP discoverable mode, the remote devices that
     * send scan request packets to the advertiser are reported back to the application through
     * Scan Response event. This is so called Enhanced Broadcasting mode.
     */
    GAP_ENHANCED_BROADCASTING(0x0080);

    /**
     * A mapping between the integer code and its corresponding type to
     * facilitate lookup by code.
     */
    private static Map<Integer, GapDiscoverMode> codeMapping;

    private int key;

    private GapDiscoverMode(int key) {
        this.key = key;
    }

    private static void initMapping() {
        codeMapping = new HashMap<Integer, GapDiscoverMode>();
        for (GapDiscoverMode s : values()) {
            codeMapping.put(s.key, s);
        }
    }

    /**
     * Lookup function based on the type code. Returns null if the code does not exist.
     *
     * @param gapDiscoverMode
     *            the code to lookup
     * @return enumeration value.
     */
    public static GapDiscoverMode getGapDiscoverMode(int gapDiscoverMode) {
        if (codeMapping == null) {
            initMapping();
        }

        if (codeMapping.get(gapDiscoverMode) == null) {
            return UNKNOWN;
        }

        return codeMapping.get(gapDiscoverMode);
    }

    /**
     * Returns the BlueGiga protocol defined value for this enum
     *
     * @return the BGAPI enumeration key
     */
    public int getKey() {
        return key;
    }
}
