package org.openhab.binding.eagle200.protocol;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Variable")
public class Variable {

    @XStreamAlias("Name")
    private String name;

    @XStreamAlias("Value")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Variable() {
    }

}
