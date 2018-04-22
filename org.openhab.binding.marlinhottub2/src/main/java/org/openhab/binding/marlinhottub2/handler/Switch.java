package org.openhab.binding.marlinhottub2.handler;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Switch {

    @XStreamAsAttribute
    public String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}