package org.openhab.binding.eagle200.protocol;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("DeviceList")
public class DeviceList {

    @XStreamImplicit(itemFieldName = "Device")
    private List<Device> devices;

    public DeviceList() {
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }
}
