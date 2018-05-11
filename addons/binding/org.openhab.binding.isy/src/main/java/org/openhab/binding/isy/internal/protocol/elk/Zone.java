package org.openhab.binding.isy.internal.protocol.elk;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("zone")
public class Zone {

    @XStreamAsAttribute
    private int id;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private int alarmDef;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAlarmDef() {
        return alarmDef;
    }

    public void setAlarmDef(int alarmDef) {
        this.alarmDef = alarmDef;
    }

}