package org.openhab.binding.isy.internal;

public interface NodeAddress {

    public static String stripDeviceId(String insteonAddress) {
        String mAddressChar = insteonAddress.substring(0, 1);
        if (mAddressChar.equals("Z")) {
            return ZWaveAddress.stripDeviceId(insteonAddress);
        } else {
            return InsteonAddress.stripDeviceId(insteonAddress);
        }
    }

    public static NodeAddress parseAddressString(String address) {
        if (address.startsWith("Z")) {
            return new ZWaveAddress(address);
        } else {
            return new InsteonAddress(address);
        }
    }

    public static NodeAddress parseAddressString(String address, int deviceId) {
        if (address.startsWith("Z")) {
            return new ZWaveAddress(address, deviceId);
        } else {
            return new InsteonAddress(address, deviceId);
        }
    }

    public String toStringNoDeviceId();

    public int getDeviceId();

    @Override
    public String toString();
}
