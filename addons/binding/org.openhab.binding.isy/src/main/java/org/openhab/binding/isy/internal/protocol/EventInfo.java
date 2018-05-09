package org.openhab.binding.isy.internal.protocol;

import org.openhab.binding.isy.internal.protocol.elk.AreaEvent;
import org.openhab.binding.isy.internal.protocol.elk.ZoneEvent;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("eventInfo")
public class EventInfo {

    @XStreamAlias("ae")
    private AreaEvent areaEvent;

    @XStreamAlias("groupName")
    private String groupName;

    @XStreamAlias("movedNode")
    private String movedNode;

    @XStreamAlias("newName")
    private String newName;

    @XStreamAlias("node")
    private EventNode node;

    @XStreamAlias("removedNode")
    private String removedNode;

    @XStreamAlias("var")
    private VariableEvent variableEvent;

    @XStreamAlias("ze")
    private ZoneEvent zoneEvent;

    public AreaEvent getAreaEvent() {
        return areaEvent;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getMovedNode() {
        return movedNode;
    }

    public String getNewName() {
        return newName;
    }

    public EventNode getNode() {
        return node;
    }

    public String getRemovedNode() {
        return removedNode;
    }

    public VariableEvent getVariableEvent() {
        return variableEvent;
    }

    public ZoneEvent getZoneEvent() {
        return zoneEvent;
    }

    public void setAreaEvent(AreaEvent areaEvent) {
        this.areaEvent = areaEvent;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setMovedNode(String movedNode) {
        this.movedNode = movedNode;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public void setNode(EventNode node) {
        this.node = node;
    }

    public void setRemovedNode(String removedNode) {
        this.removedNode = removedNode;
    }

    public void setVariableEvent(VariableEvent variableEvent) {
        this.variableEvent = variableEvent;
    }

    public void setZoneEvent(ZoneEvent zoneEvent) {
        this.zoneEvent = zoneEvent;
    }

}