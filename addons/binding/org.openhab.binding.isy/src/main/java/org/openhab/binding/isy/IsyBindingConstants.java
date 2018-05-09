/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.isy;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link IsyBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Craig Hamilton - Initial contribution
 */
public class IsyBindingConstants {

    public static final String UPNP_DISCOVERY_KEY = "isy 994i";

    public static final String BINDING_ID = "isy";

    public final static ThingTypeUID THING_TYPE_ISYBRIDGE = new ThingTypeUID(BINDING_ID, "isyBridge");

    // List of all Thing Type UIDs

    public final static ThingTypeUID MOTION_THING_TYPE = new ThingTypeUID(BINDING_ID, "motion");
    public final static ThingTypeUID PROGRAM_THING_TYPE = new ThingTypeUID(BINDING_ID, "program");
    public final static ThingTypeUID SCENE_THING_TYPE = new ThingTypeUID(BINDING_ID, "scene");
    public final static ThingTypeUID VARIABLE_THING_TYPE = new ThingTypeUID(BINDING_ID, "variable");

    public final static ThingTypeUID DIMMER_THING_TYPE = new ThingTypeUID(BINDING_ID, "dimmer");
    public final static ThingTypeUID SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, "switch");
    public final static ThingTypeUID LEAKDETECTOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "leakdetector");
    public final static ThingTypeUID RELAY_THING_TYPE = new ThingTypeUID(BINDING_ID, "relay");
    public final static ThingTypeUID GARAGEDOORKIT_THING_TYPE = new ThingTypeUID(BINDING_ID, "garage");
    public final static ThingTypeUID KEYPAD_LINC_6_THING_TYPE = new ThingTypeUID(BINDING_ID, "keypadlinc6");
    public final static ThingTypeUID KEYPAD_LINC_5_THING_TYPE = new ThingTypeUID(BINDING_ID, "keypadlinc5");
    public final static ThingTypeUID REMOTELINC_8_THING_TYPE = new ThingTypeUID(BINDING_ID, "remotelinc8");
    public final static ThingTypeUID KEYPADLINC_8_THING_TYPE = new ThingTypeUID(BINDING_ID, "keypadlinc8");
    public final static ThingTypeUID INLINELINC_SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, "inlinelincswitch");
    public final static ThingTypeUID OUTLETLINC_DIMMER_THING_TYPE = new ThingTypeUID(BINDING_ID, "outletlinc");
    public final static ThingTypeUID OUTLETLINC_DUAL_THING_TYPE = new ThingTypeUID(BINDING_ID, "dualoutletlinc");
    public final static ThingTypeUID FANLINC_THING_TYPE = new ThingTypeUID(BINDING_ID, "fanlinc");
    public final static ThingTypeUID SMOKE_DETECTOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "smokedetector");
    public final static ThingTypeUID VENSTAR_THERMOSTAT_THING_TYPE = new ThingTypeUID(BINDING_ID, "venstar_thermostat");
    public final static ThingTypeUID EZX10_RF_THING_TYPE = new ThingTypeUID(BINDING_ID, "ezx10_rf");

    public final static ThingTypeUID TRIGGERLINC_THING_TYPE = new ThingTypeUID(BINDING_ID, "triggerlinc");
    public final static ThingTypeUID TOGGLELINC_THING_TYPE = new ThingTypeUID(BINDING_ID, "togglelinc");
    public final static ThingTypeUID HIDDENDOORSENSOR_THING_TYPE = new ThingTypeUID(BINDING_ID, "hiddendoorsensor");

    public final static ThingTypeUID UNRECOGNIZED_SWITCH_THING_TYPE = new ThingTypeUID(BINDING_ID, "unrecognized");

    public final static String BRIDGE_CONFIG_IPADDRESS = "ipAddress";
    public final static String BRIDGE_CONFIG_SERIALNUMBER = "serial";
    public final static String BRIDGE_CONFIG_USER = "user";
    public final static String BRIDGE_CONFIG_PASSWORD = "password";

    // List of all Channel ids
    // public final static String CHANNEL_ONOFFSENSOR = "OL";
    public final static String CHANNEL_DIMMERLEVEL = "loadlevel";
    public final static String CHANNEL_PADDLEACTION = "paddleaction";
    public final static String CHANNEL_CONTROL_ACTION = "control";

    public final static String CHANNEL_SWITCH = "state";
    // motion
    public final static String CHANNEL_MOTION_MOTION = "motion";
    public final static String CHANNEL_MOTION_DUSK = "dusk_dawn";
    public final static String CHANNEL_MOTION_BATTERY = "low_battery";
    // garage
    public final static String CHANNEL_GARAGE_CONTACT = "relay";
    public final static String CHANNEL_GARAGE_SENSOR = "contactSensor";
    // leak
    public final static String CHANNEL_LEAK_DRY = "dry";
    public final static String CHANNEL_LEAK_WET = "wet";
    public final static String CHANNEL_HEARTBEAT = "heartbeat";

    public final static String CHANNEL_KEYPAD_LINC_A = "button_a";
    public final static String CHANNEL_KEYPAD_LINC_B = "button_b";
    public final static String CHANNEL_KEYPAD_LINC_C = "button_c";
    public final static String CHANNEL_KEYPAD_LINC_D = "button_d";
    public final static String CHANNEL_KEYPAD_LINC_E = "button_e";
    public final static String CHANNEL_KEYPAD_LINC_F = "button_f";
    public final static String CHANNEL_KEYPAD_LINC_G = "button_g";
    public final static String CHANNEL_KEYPAD_LINC_H = "button_h";

    // program
    public final static String CHANNEL_PROGRAM_CONTROL = "control";
    public final static String CHANNEL_PROGRAM_RUN_IF = "run";
    public final static String CHANNEL_PROGRAM_RUN_THEN = "runThen";
    public final static String CHANNEL_PROGRAM_RUN_ELSE = "runElse";
    public final static String CHANNEL_PROGRAM_STOP = "stop";
    // variables
    public final static String CHANNEL_VARIABLE_VALUE = "value";

    public final static String CHANNEL_SCENE_ONOFF = "onoff";

    public final static String CHANNEL_OPEN_SENSOR = "open_sensor";
    public final static String CHANNEL_CLOSED_SENSOR = "closed_sensor";

    public final static String CHANNEL_LOAD = "load";
    public final static String CHANNEL_LOAD2 = "load2";

    public final static String CHANNEL_SMOKEDETECT_SMOKE = "smokesensor_smoke";
    public final static String CHANNEL_SMOKEDETECT_CO = "smokesensor_co";
    public final static String CHANNEL_SMOKEDETECT_TEST = "smokesensor_test";
    public final static String CHANNEL_SMOKEDETECT_UNKNOWNMESSAGE = "smokesensor_unknown_message";
    public final static String CHANNEL_SMOKEDETECT_CLEAR = "smokesensor_clear";
    public final static String CHANNEL_SMOKEDETECT_LOWBAT = "smokesensor_lowbattery";
    public final static String CHANNEL_SMOKEDETECT_MALFUNCTION = "smokesensor_malfunction";

    public final static String CHANNEL_VENSTAR_TEMP = "venstar_temp";
    public final static String CHANNEL_VENSTAR_HUMID = "venstar_humidity";
    public final static String CHANNEL_VENSTAR_COOLSET = "venstar_coolsetpoint";
    public final static String CHANNEL_VENSTAR_HEATSET = "venstar_heatsetpoint";
    public final static String CHANNEL_VENSTAR_MODE = "venstar_mode";// 0 = off, 1 = heat, 2 = cool, 3 = auto, 5 = prog
                                                                     // auto, 6 = prog heat, 7 = prog cool,
    public final static String CHANNEL_VENSTAR_FAN = "venstar_fan"; // fan setting: 7 = ON, 8 = Auto
    public final static String CHANNEL_VENSTAR_HEATCOOL = "venstar_heatcoolstate"; // 0 = Off, 1 = heat on, 2 = cool on
    public final static String CHANNEL_VENSTAR_UOM = "venstar_uom";
    public final static String CHANNEL_VENSTAR_MAIN = "venstar_main";
    public final static String CHANNEL_VENSTAR_COOLCONTROL = "venstar_coolcontrol";
    public final static String CHANNEL_VENSTAR_HEATCONTROL = "venstar_heatcontrol";
    public final static String CHANNEL_VENSTAR_FANCONTROL = "venstar_fancontrol";

}
