package org.openhab.binding.eagle200.protocol;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Component")
public class Component {

    @XStreamAlias("Variables")
    private Variables variables;

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    public Component() {
    }
}
