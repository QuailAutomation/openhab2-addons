package org.openhab.binding.eagle200.protocol;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Device")
public class DeviceQuery {
    @XStreamAlias("Components")
    private Components components;

    public Components getComponents() {
        return components;
    }

    public void setComponents(Components components) {
        this.components = components;
    }

}
