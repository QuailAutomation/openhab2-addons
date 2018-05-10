package org.openhab.binding.isy.internal.protocol;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("CList")
public class VariableList {

    @XStreamImplicit(itemFieldName = "e")
    private List<StateVariable> stateVariables;

    /**
     * @return the stateVariables
     */
    public List<StateVariable> getStateVariables() {
        return stateVariables;
    }

    /**
     * @param stateVariables the stateVariables to set
     */
    public void setStateVariables(List<StateVariable> stateVariables) {
        this.stateVariables = stateVariables;
    }

}