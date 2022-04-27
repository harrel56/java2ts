package org.harrel.java2ts;

import java.util.List;

public class GenericType implements TsType {

    private final String name;
    private final List<TsType> bound;

    public GenericType(String name, List<TsType> bounds) {
        this.name = name;
        this.bound = bounds;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    public String getBoundedTypeName() {
        String boundString = bound.isEmpty() ? "" : " extends " + bound.get(0).getTypeName();
        return getTypeName() + boundString;
    }
}
