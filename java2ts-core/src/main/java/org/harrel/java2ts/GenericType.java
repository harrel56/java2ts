package org.harrel.java2ts;

import java.util.List;
import java.util.stream.Collectors;

class GenericType implements TsType {

    private final String name;
    private final List<TsType> bounds;

    public GenericType(String name, List<TsType> bounds) {
        this.name = name;
        this.bounds = bounds;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    public String getBoundedTypeName() {
        String boundString = bounds.isEmpty() ? "" : " extends " + getBoundsString();
        return getTypeName() + boundString;
    }

    private String getBoundsString() {
        return bounds.stream()
                .map(TsType::getTypeName)
                .collect(Collectors.joining(" & "));
    }
}
