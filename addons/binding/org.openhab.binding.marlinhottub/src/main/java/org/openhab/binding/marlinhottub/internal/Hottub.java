package org.openhab.binding.marlinhottub.internal;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("hottub")
public class Hottub {

    @XStreamAlias("pump")
    private Switch pump;

    @XStreamAlias("blower")
    private Switch blower;

    @XStreamAlias("heater")
    private Switch heater;

    @XStreamAlias("temperature")
    private Temperature temperature;

    @XStreamAlias("setpoint")
    private Temperature setpoint;

    public Switch getPump() {
        return pump;
    }

    public void setPump(Switch pump) {
        this.pump = pump;
    }

    public Switch getBlower() {
        return blower;
    }

    public void setBlower(Switch blower) {
        this.blower = blower;
    }

    public Switch getHeater() {
        return heater;
    }

    public void setHeater(Switch heater) {
        this.heater = heater;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public Temperature getSetpoint() {
        return setpoint;
    }

    public void setSetpoint(Temperature setpoint) {
        this.setpoint = setpoint;
    }
}
