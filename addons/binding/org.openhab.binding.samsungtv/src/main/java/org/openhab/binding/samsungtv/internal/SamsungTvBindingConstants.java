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
package org.openhab.binding.samsungtv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SamsungTvBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class SamsungTvBindingConstants {

    public static final String BINDING_ID = "samsungtv";

    public static final ThingTypeUID SAMSUNG_TV_THING_TYPE = new ThingTypeUID(BINDING_ID, "tv");

    // List of all remote controller thing channel id's
    public static final String KEY_CODE = "keyCode";
    public static final String POWER = "power";

    // List of all media renderer thing channel id's
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";
    public static final String BRIGHTNESS = "brightness";
    public static final String CONTRAST = "contrast";
    public static final String SHARPNESS = "sharpness";
    public static final String COLOR_TEMPERATURE = "colorTemperature";

    // List of all main TV server thing channel id's
    public static final String SOURCE_NAME = "sourceName";
    public static final String SOURCE_ID = "sourceId";
    public static final String CHANNEL = "channel";
    public static final String PROGRAM_TITLE = "programTitle";
    public static final String CHANNEL_NAME = "channelName";
    public static final String BROWSER_URL = "url";
    public static final String STOP_BROWSER = "stopBrowser";
}
