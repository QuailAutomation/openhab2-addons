package org.openhab.binding.isy.internal;

import java.util.List;

public class Scene {

    public String name;
    public String address;
    public List<String> links;

    public Scene(String name, String address, List<String> links) {
        this.name = name;
        this.address = address;
        this.links = links;
    }

}
