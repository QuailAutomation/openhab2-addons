/**
 * Copyright (c) 2014,2017 by the respective copyright holders.
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
package org.openhab.binding.marlinfireplace;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MarlinFireplaceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@NonNullByDefault
public class MarlinFireplaceBindingConstants {

    private static final String BINDING_ID = "marlinfireplace";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FIREPLACE = new ThingTypeUID(BINDING_ID, "fireplace");

    // List of all Channel ids
    public static final String TEMPERATURE = "temperature";
    public static final String SETPOINT = "setpoint";
    public static final String MODE = "mode";
    public static final String STATE = "state";

}
