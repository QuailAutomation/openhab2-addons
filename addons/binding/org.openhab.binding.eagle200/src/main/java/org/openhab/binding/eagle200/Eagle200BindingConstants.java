/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.eagle200;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Eagle200BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@NonNullByDefault
public class Eagle200BindingConstants {

    public static final String BINDING_ID = "eagle200";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_EAGLE200_BRIDGE = new ThingTypeUID(BINDING_ID, "eagle200_bridge");
    public static final ThingTypeUID THING_TYPE_EAGLE200_METER = new ThingTypeUID(BINDING_ID,
            "eagle200_electric_meter");

    public static final String THING_BRIDGECONFIG_HOSTNAME = "hostname";
    public static final String THING_BRIDGECONFIG_CLOUDID = "cloudid";
    public static final String THING_BRIDGECONFIG_INSTALLCODE = "installcode";

    public static final String THING_CONFIG_HWADDRESS = "hwaddress";
    public static final String THING_CONFIG_SCANFREQUENCY = "frequency";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(new ThingTypeUID[] { THING_TYPE_EAGLE200_BRIDGE, THING_TYPE_EAGLE200_METER }));

}
