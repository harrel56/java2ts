package org.harrel.java2ts;

import java.util.Comparator;

record NamedProperty(String name, TsType type) implements Comparable<NamedProperty> {

    @Override
    public int compareTo(NamedProperty o) {
        var comp = Comparator.comparing(NamedProperty::name);
        if(type instanceof FunctionType && o.type instanceof FunctionType) {
            comp = comp.thenComparing(p -> ((FunctionType) p.type).getArgsSize());
        }
        return comp.compare(this, o);
    }

    @Override
    public String toString() {
        return name + ": " + type.getNullableTypeName();
    }
}
