package org.openhab.binding.eagle200.protocol;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("Variables")
public class Variables {

    @XStreamImplicit(itemFieldName = "Variable")
    private List<Variable> variables;

    public Variables() {
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }
}
