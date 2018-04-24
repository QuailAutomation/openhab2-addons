package org.openhab.binding.isy.internal.protocol.elk;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("status")
public class ElkStatus {

    @XStreamOmitField
    private long timestamp;

    @XStreamImplicit(itemFieldName = "ae")
    private List<AreaEvent> areas;

    @XStreamImplicit(itemFieldName = "ze")
    private List<ZoneEvent> zones;

    public List<AreaEvent> getAreas() {
        return areas;
    }

    public void setAreas(List<AreaEvent> areas) {
        this.areas = areas;
    }

    public List<ZoneEvent> getZones() {
        return zones;
    }

    public void setZones(List<ZoneEvent> zones) {
        this.zones = zones;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

}