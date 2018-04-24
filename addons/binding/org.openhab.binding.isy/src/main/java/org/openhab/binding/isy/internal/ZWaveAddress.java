package org.openhab.binding.isy.internal;

public class ZWaveAddress implements NodeAddress {

    String mZByte1;
    String mAddressChar;
    int mZDeviceId;
    private static int UNSPECIFIED_DEVICE_ID = 1243345;

    public ZWaveAddress(String address, int deviceId) {
        mZByte1 = address.substring(0, 5);
        mZDeviceId = deviceId;
    }

    public ZWaveAddress(String address) {
        mZByte1 = address.substring(0, 5);
        String[] addressParts = address.split("_");
        int deviceId = Integer.parseInt(addressParts[1]);
        if (deviceId > 0) {
            mZDeviceId = deviceId;
        } else {
            mZDeviceId = UNSPECIFIED_DEVICE_ID;
        }
    }

    @Override
    public String toStringNoDeviceId() {
        return new StringBuilder().append(mZByte1).toString();
    }

    @Override
    public int getDeviceId() {
        return mZDeviceId;
    }

    public boolean matchesExcludingDeviceId(String address) {
        String[] addressParts = address.split("_");
        return mZByte1.equals(addressParts[0]);
    }

    public boolean matchesExcludingDeviceId(ZWaveAddress address) {
        return address.mZByte1.equals(mZByte1);
    }

    public static String stripDeviceId(String deviceAddress) {
        String[] addressParts = deviceAddress.split("_");
        return new StringBuilder().append(addressParts[0]).toString();
    }

    // TODO implement hashCode?
    private String pad(String theByte) {
        if (theByte.length() == 1) {
            return "0" + theByte;
        } else {
            return theByte;
        }
    }

    public String toStringPaddedBytes() {
        return new StringBuilder().append(pad(mZByte1)).append("_").append(mZDeviceId).toString();
    }

    @Override
    public String toString() {
        return new StringBuilder().append(mZByte1).append("_").append(mZDeviceId).toString();
    }
}