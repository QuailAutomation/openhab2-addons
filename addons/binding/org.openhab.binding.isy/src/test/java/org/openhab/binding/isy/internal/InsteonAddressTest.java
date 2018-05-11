package org.openhab.binding.isy.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InsteonAddressTest {

    @Test
    public void testHandleAforDeviceId() {
        InsteonAddress address = new InsteonAddress("41 EA 6 A");
        assertEquals(address.toStringNoDeviceId(), "41 EA 6");
        assertEquals(address.toStringPaddedBytes(), "41 EA 06 A");
        assertEquals(address.toString(), "41 EA 6 A");
    }

}
