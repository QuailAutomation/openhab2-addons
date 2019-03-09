/**
 *
 */
package org.openhab.binding.isy.internal.protocol;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author thomas hentschel
 *
 */
@XStreamAlias("nodeInfo")
public class NodeInfo {

    @XStreamAlias("node")
    private Node node;

    @XStreamAlias("properties")
    private Properties properties;

    /**
     *
     */
    public NodeInfo() {
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return this.node;
    }

    public Properties getProperties() {
        return this.properties;
    }

}
