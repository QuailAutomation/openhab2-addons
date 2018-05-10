package org.openhab.binding.isy.internal.protocol;

import java.util.List;
import java.util.Map;

import org.openhab.binding.isy.internal.NodeAddress;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("nodes")
public class Nodes {

    @XStreamOmitField
    private Map<NodeAddress, Node> nodeMap;

    @XStreamImplicit(itemFieldName = "node")
    private List<Node> nodes;

    @XStreamImplicit(itemFieldName = "group")
    private List<Group> groups;

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    // public void index() {
    // if (nodes != null) {
    // Map<NodeAddress, Node> newMap = Maps.newHashMap();
    // for (Node node : this.nodes) {
    // try {
    // String address = node.getAddress() != null ? node.getAddress() : node.getId();
    // InsteonAddressChannel addressChannel = InsteonAddress.parseNodeAddressChannel(address);
    // newMap.put(addressChannel, node);
    // } catch (Exception e) {
    // // skip
    // }
    // }
    // nodeMap = newMap;
    // }
    // }

    public Node getNode(NodeAddress address) {
        return nodeMap != null ? nodeMap.get(address) : null;
    }

}