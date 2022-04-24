package org.harrel.java2ts;

public class GenericType implements TsType {

    private final String name;

    public GenericType(String name) {
        this.name = name;
    }

    @Override
    public String getTypeName() {
        return name;
    }
}
