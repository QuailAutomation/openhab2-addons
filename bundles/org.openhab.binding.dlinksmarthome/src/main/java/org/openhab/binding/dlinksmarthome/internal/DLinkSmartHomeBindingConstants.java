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
package org.openhab.binding.dlinksmarthome.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DLinkSmartHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class DLinkSmartHomeBindingConstants {

    public static final String BINDING_ID = "dlinksmarthome";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DCHS150 = new ThingTypeUID(BINDING_ID, "DCH-S150");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_DCHS150);

    // Motion trigger channel
    public static final String MOTION = "motion";
}
