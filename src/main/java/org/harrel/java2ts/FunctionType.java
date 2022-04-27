package org.harrel.java2ts;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionType implements TsType {

    private final TsType returnType;
    private final List<TsType> genericTypes;

    private final Map<String, TsType> arguments;

    public FunctionType(TsType returnType, List<TsType> genericTypes, Map<String, TsType> arguments) {
        this.returnType = returnType;
        this.genericTypes = genericTypes;
        this.arguments = arguments;
    }

    @Override
    public String getTypeName() {
        String genericTypesString = genericTypes.stream()
                .map(GenericType.class::cast)
                .map(GenericType::getBoundedTypeName)
                .collect(Collectors.joining(", "));
        if(!genericTypesString.isEmpty()) {
            genericTypesString = "<" + genericTypesString + ">";
        }

        String argsString = arguments.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().getNullableTypeName())
                .collect(Collectors.joining(", "));
        return "%s(%s) => %s".formatted(genericTypesString, argsString, returnType.getNullableTypeName());
    }

    @Override
    public String getNullableTypeName() {
        return "(%s) | null".formatted(getTypeName());
    }
}
