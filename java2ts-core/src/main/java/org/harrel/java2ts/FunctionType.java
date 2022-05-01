package org.harrel.java2ts;

import java.util.List;
import java.util.stream.Collectors;

class FunctionType implements TsType {

    private final TsType returnType;
    private final List<TsType> genericTypes;
    private final List<NamedProperty> arguments;

    public FunctionType(TsType returnType, List<TsType> genericTypes, List<NamedProperty> arguments) {
        this.returnType = returnType;
        this.genericTypes = genericTypes;
        this.arguments = arguments;
    }

    @Override
    public String getTypeName() {
        String genericTypesString = getGenericTypesString();
        String argsString = getArgsString();
        return "%s(%s): %s".formatted(genericTypesString, argsString, returnType.getNullableTypeName());
    }

    @Override
    public String getNullableTypeName() {
        return "(%s) | null".formatted(getTypeNameAsLambda());
    }

    public int getArgsSize() {
        return arguments.size();
    }

    public String getTypeNameAsLambda() {
        String genericTypesString = getGenericTypesString();
        String argsString = getArgsString();
        return "%s(%s) => %s".formatted(genericTypesString, argsString, returnType.getNullableTypeName());
    }

    private String getGenericTypesString() {
        String genericTypesString = genericTypes.stream()
                .map(GenericType.class::cast)
                .map(GenericType::getBoundedTypeName)
                .collect(Collectors.joining(", "));
        if (!genericTypesString.isEmpty()) {
            genericTypesString = "<" + genericTypesString + ">";
        }
        return genericTypesString;
    }

    private String getArgsString() {
        return arguments.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }
}
