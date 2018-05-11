package org.openhab.binding.isy.internal;

public enum VariableType {
    INTEGER(1),
    STATE(2);

    private int type;

    VariableType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static VariableType fromInt(int id) {
        for (VariableType variableType : VariableType.values()) {
            if (id == variableType.getType()) {
                return variableType;
            }
        }
        throw new IllegalArgumentException("Id: " + id + " is not valid for VariableType, must be 1 or 2");
    }

    // public static VariableType fromId(String type) {
    // for (VariableType variableType : VariableType.values()) {
    // if (type.equals(variableType.getType())) {
    // return variableType;
    // }
    // }
    // throw new IllegalArgumentException("Id: " + type + " is not valid for VariableType, must be 1 or 2");
    // }
}
