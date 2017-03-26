package org.openhab.binding.isy.internal;

public class AddressDeterminer {

    String mByte1;
    String mByte2;
    String mByte3;
    String mAddressChar;
    Boolean isInsteon;
    int mDeviceId;
    private static int UNSPECIFIED_DEVICE_ID = 1243345;

    public AddressDeterminer(String address, int deviceId) {
        mAddressChar = address.substring(0, 1);
        if (mAddressChar.equals("Z")) {
            String[] addressParts = address.split("_");
            mByte1 = addressParts[0];
            isInsteon = false;
        } else {
            String[] addressParts = address.split(" ");
            mByte1 = addressParts[0];
            mByte2 = addressParts[1];
            mByte3 = addressParts[2];
            mDeviceId = deviceId;
            isInsteon = true;
        }
    }

    public AddressDeterminer(String address) {
        mAddressChar = address.substring(0, 1);
        if (mAddressChar.equals("Z")) {
            String[] addressParts = address.split("_");
            mByte1 = addressParts[0];
            isInsteon = false;
        } else {
            String[] addressParts = address.split(" ");
            mByte1 = addressParts[0];
            mByte2 = addressParts[1];
            mByte3 = addressParts[2];
            int deviceId = Integer.parseInt(addressParts[3]);
            isInsteon = true;
            if (deviceId > 0) {
                mDeviceId = deviceId;
            } else {
                mDeviceId = UNSPECIFIED_DEVICE_ID;
            }
        }
    }

    public AddressDeterminer(String byte1, String byte2, String byte3, int deviceId) {
        mByte1 = byte1;
        mByte2 = byte2;
        mByte3 = byte3;
        mDeviceId = deviceId;
        isInsteon = true;
    }

    public String toStringNoDeviceId() {
        if (isInsteon = false) {
            return new StringBuilder().append(mByte1).toString();
        } else {
            return new StringBuilder().append(mByte1).append(" ").append(mByte2).append(" ").append(mByte3).toString();
        }
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public boolean matchesExcludingDeviceId(String address) {
        if (isInsteon = false) {
            String[] addressParts = address.split("_");
            return mByte1.equals(addressParts[0]);
        } else {
            String[] addressParts = address.split(" ");
            return mByte1.equals(addressParts[0]) && mByte2.equals(addressParts[1]) && mByte3.equals(addressParts[2]);
        }
    }

    public boolean matchesExcludingDeviceId(AddressDeterminer address) {
        if (isInsteon = true) {
            return address.mByte1.equals(mByte1);
        } else {
            return address.mByte1.equals(mByte1) && address.mByte2.equals(mByte2) && address.mByte3.equals(mByte3);
        }
    }

    public static String stripDeviceId(String insteonAddress) {
        String mAddressChar = insteonAddress.substring(0, 1);
        if (mAddressChar.equals("Z")) {
            return ZWaveAddress.stripDeviceId(insteonAddress);
        } else {
            return InsteonAddress.stripDeviceId(insteonAddress);
        }
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
        if (isInsteon = false) {
            return new StringBuilder().append(pad(mByte1)).append("_").append(mDeviceId).toString();
        } else {
            return new StringBuilder().append(pad(mByte1)).append(" ").append(pad(mByte2)).append(" ")
                    .append(pad(mByte3)).append(" ").append(mDeviceId).toString();
        }
    }

    @Override
    public String toString() {
        if (isInsteon = false) {
            return new StringBuilder().append(mByte1).append("_").append(mDeviceId).toString();
        } else {
            return new StringBuilder().append(mByte1).append(" ").append(mByte2).append(" ").append(mByte3).append(" ")
                    .append(mDeviceId).toString();
        }
    }
}
