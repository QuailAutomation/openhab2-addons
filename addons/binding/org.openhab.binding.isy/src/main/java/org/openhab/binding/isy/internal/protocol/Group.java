package org.openhab.binding.isy.internal.protocol;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("group")
public class Group {

    private String address;

    private String name;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}