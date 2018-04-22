package org.openhab.binding.marlinhottub2.handler;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Temperature {

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XStreamAsAttribute
    public String unit;

    @XStreamAsAttribute
    public String value;
}