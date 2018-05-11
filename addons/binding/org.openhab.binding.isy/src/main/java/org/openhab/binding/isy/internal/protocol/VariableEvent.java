package org.openhab.binding.isy.internal.protocol;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("var")
public class VariableEvent {

    @XStreamAsAttribute
    private int type;

    @XStreamAsAttribute
    private int id;

    @XStreamAsAttribute
    private int val;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int zone) {
        this.id = zone;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "VariableEvent [type=" + type + ", id=" + id + ", val=" + val + "]";
    }

}