package org.openhab.binding.isy.internal;

import java.util.Collection;
import java.util.List;

import org.openhab.binding.isy.internal.protocol.NodeInfo;
import org.openhab.binding.isy.internal.protocol.Properties;
import org.openhab.binding.isy.internal.protocol.Property;
import org.openhab.binding.isy.internal.protocol.VariableEvent;
import org.openhab.binding.isy.internal.protocol.VariableList;

public interface OHIsyClient {
    // public void connect();
    //
    // public void disconnect();

    public boolean changeNodeState(String command, String value, String address);

    public boolean changeNodeProperty(String property, String value, String address);

    public boolean changeVariableState(VariableType type, int id, int value);

    public boolean changeSceneState(String address, int value);

    public boolean changeProgramState(String programId, String command);

    public List<Node> getNodes();

    public Collection<Program> getPrograms();

    public VariableList getVariableDefinitions(VariableType type);

    public List<Scene> getScenes();

    public Property getNodeStatus(String node);

    public Property getNodeStatus(String node, String propertyName);

    public NodeInfo getNodeInfo(String node);

    public Properties getNodeProperties(String node);

    public VariableEvent getVariableValue(VariableType type, int id);

}
