package org.harrel.java2ts;

import java.util.Map;
import java.util.stream.Collectors;

public class FunctionType implements TsType {

    private final TsType returnType;
    private final Map<String, TsType> arguments;

    public FunctionType(TsType returnType, Map<String, TsType> arguments) {
        this.returnType = returnType;
        this.arguments = arguments;
    }

    @Override
    public String getTypeName() {
        String argsString = arguments.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().getNullableTypeName())
                .collect(Collectors.joining(", "));
        return "(%s) => %s".formatted(argsString, returnType.getNullableTypeName());
    }

    @Override
    public String getNullableTypeName() {
        return "(%s) | null".formatted(getTypeName());
    }
}
