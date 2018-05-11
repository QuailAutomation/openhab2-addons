package org.openhab.binding.isy.config;

public class IsyVariableConfiguration {

    public static final String ID = "id";
    public static final String TYPE = "type";

    public int id;
    public int type;

    @Override
    public String toString() {
        return "[IsyVariableConfiguration] id: " + id + ", type: " + type;
    }

}
