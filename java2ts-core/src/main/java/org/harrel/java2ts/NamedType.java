package org.harrel.java2ts;

import java.util.List;
import java.util.stream.Collectors;

abstract class NamedType implements TsType {

    protected final String name;
    protected final List<TsType> genericTypes;

    public NamedType(String name, List<TsType> genericTypes) {
        this.name = name;
        this.genericTypes = genericTypes;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    public abstract String getTypeDeclaration();

    String getGenericTypesString() {
        if(genericTypes.isEmpty()) {
            return "";
        }

        return genericTypes.stream()
                .map(GenericType.class::cast)
                .map(GenericType::getBoundedTypeName)
                .collect(Collectors.joining(", ", "<", ">"));
    }
}
