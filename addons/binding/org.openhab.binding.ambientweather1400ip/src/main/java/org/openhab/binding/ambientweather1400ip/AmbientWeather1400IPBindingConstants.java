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
package org.openhab.binding.ambientweather1400ip;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AmbientWeather1400IPBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Hentschel - Initial contribution
 */
@NonNullByDefault
public class AmbientWeather1400IPBindingConstants {

    private static final String BINDING_ID = "ambientweather1400ip";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AMBIENTWEATHER1400IP = new ThingTypeUID(BINDING_ID, "weatherstation");

    // List of all Channel ids
    public static final String INDOOR_TEMP = "indoor_temp";
    public static final String OUTDOOR_TEMP = "outdoor_temp";
    public static final String INDOOR_HUMIDITY = "indoor_humidity";
    public static final String OUTDOOR_HUMIDITY = "outdoor_humidity";
    public static final String ABS_PRESSURE = "abs_pressure";
    public static final String REL_PRESSURE = "rel_pressure";
    public static final String WIND_DIRECTION = "wind_direction";
    public static final String WIND_SPEED = "wind_speed";
    public static final String WIND_GUST = "wind_gust";
    public static final String SOLAR_RADIATION = "solar_radiation";
    public static final String UV = "uv";
    public static final String UVI = "uvi";
    public static final String HOURLY_RAIN = "hourly_rain";
    public static final String DAILY_RAIN = "daily_rain";
    public static final String WEEKLY_RAIN = "weekly_rain";
    public static final String MONTHLY_RAIN = "monthly_rain";
    public static final String YEARLY_RAIN = "yearly_rain";
    public static final String BATTERY_IN = "battery_in";
    public static final String BATTERY_OUT = "battery_out";

    public static final String CONFIG_HOSTNAME = "hostname";
    public static final String CONFIG_SCANRATE = "scanrate";
}
