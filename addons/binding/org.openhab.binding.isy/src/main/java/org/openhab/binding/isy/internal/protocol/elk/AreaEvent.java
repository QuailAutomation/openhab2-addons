package org.openhab.binding.isy.internal.protocol.elk;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("ae")
public class AreaEvent {

    @XStreamAsAttribute
    private int type;

    @XStreamAsAttribute
    private int area;

    @XStreamAsAttribute
    private int val;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "AreaEvent [type=" + type + ", area=" + area + ", val=" + val + "]";
    }

}