package org.openhab.binding.isy.internal.protocol.elk;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("topology")
public class Topology {

    private Areas areas;

    public Areas getAreas() {
        return areas;
    }

    public void setAreas(Areas areas) {
        this.areas = areas;
    }

}