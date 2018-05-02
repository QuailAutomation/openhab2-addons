# Rainforest Eagle 2000 Binding

This binding connects to a Rainforest Eagle 200 device used to connect to utility meters (and other zigbee devices - not implemented). Connection is local, not thru the Rainforest cloud site.

## Supported Things

Rainforest Eagle 200 and utility meters connected thru via that device

## Discovery

The binding should be able to discover the Eagle 200 via mDNS/Bonjour on the local network. Once the bridge is configured, the utility meter(s) should be discovered automatically. 

## Binding Configuration

Once the binding discovers the Eagle 200 as a bridge device, the binding needs the install ID (IN) from the back of the device to connect and discover utility meters etc.

## Utility Meter Thing Configuration

The utility meter binding scan frequency can be configured, it defaults to 60 seconds

## Channels

Channels are discovered from the actual utility meter scan. It may vary by device.

