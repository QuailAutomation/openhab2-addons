/**
 *
 */
package org.openhab.binding.isy.internal.protocol;

/**
 * @author thomashentschel
 *
 */
public class EventNode {

    private String address;
    private String name;
    private String type;
    private boolean enabled;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Node asNode() {
        Node result = new Node();
        result.setAddress(this.getAddress());
        result.setType(this.getType());
        result.setName(this.getName());
        // result.setDevtype(this.getType());
        return result;
    }

}
