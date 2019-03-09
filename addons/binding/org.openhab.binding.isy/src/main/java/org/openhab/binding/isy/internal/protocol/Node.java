package org.openhab.binding.isy.internal.protocol;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("node")
public class Node {

    private String name;

    private String address;

    private String type;

    private DevType devtype;

    @XStreamAsAttribute
    private String id;

    @XStreamImplicit(itemFieldName = "property")
    private List<Property> properies;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DevType getDevtype() {
        return devtype;
    }

    public void setDevtype(DevType devtype) {
        this.devtype = devtype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Property> getProperies() {
        return properies;
    }

    public void setProperies(List<Property> properies) {
        this.properies = properies;
    }

    @XStreamAlias("devtype")
    public static class DevType {

        private String gen;
        private String mfg;
        private String cat;

        public String getGen() {
            return gen;
        }

        public void setGen(String gen) {
            this.gen = gen;
        }

        public String getMfg() {
            return mfg;
        }

        public void setMfg(String mfg) {
            this.mfg = mfg;
        }

        public String getCat() {
            return cat;
        }

        public void setCat(String cat) {
            this.cat = cat;
        }

    }

}