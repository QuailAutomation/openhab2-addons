package org.openhab.binding.isy.internal.protocol.elk;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("ze")
public class ZoneEvent {

    @XStreamAsAttribute
    private int type;

    @XStreamAsAttribute
    private int zone;

    @XStreamAsAttribute
    private int val;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "ZoneEvent [type=" + type + ", zone=" + zone + ", val=" + val + "]";
    }

}