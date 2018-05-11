# Marlin Fireplace Binding

This is the binding for a home-grown Fireplace controller, which is based on Raspberry Pi GPIO, OLED display, MQTT and Node-Red. Source for the actual controller is in another repository.

## Supported Things

Just supports the fireplace thing, which has current temperature read, fireplace mode read/set, set temp read/set and actuator read (fireplace on/off). The fireplace mode has to be in mode ''WEB'' in order for the set point to be accepted.

## Discovery
No discovery supported, manual configuration required. Configuration is done via paper UI. This needs a configured MQTT service to work, typically done via services/mqtt.cfg. See http://docs.openhab.org/addons/bindings/mqtt1/readme.html

## Thing Configuration

Required config:
* mqtt broker name: needs the name of the configured mqtt service to be used. Typically done via services/mqtt.cfg
* Base Topic: the mqtt base topic the fireplace sends/receives information on, i.e. ''/fireplace''

## Channels
* ''temperature'': current temperature, (number - read-only)
* ''setpoint'' : current set point, can be set while ''mode'' is '''WEB''' (number - read/write)
* ''mode'': current mode, string choice '''OFF''', '''ON''', '''LOCAL''', '''REMOTE''' or '''WEB''' (string - read/write)
* ''state'' current flame/fire state (OnOff - read-only)
