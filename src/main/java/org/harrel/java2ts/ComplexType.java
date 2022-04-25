package org.harrel.java2ts;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComplexType implements TsType {

    private static final String INDENTED_NEW_LINE = "\n    ";

    private final String name;
    private final List<GenericType> genericTypes;
    private final List<TsType> superTypes;
    private final Map<String, TsType> fields;
    private final Map<String, FunctionType> methods;

    public ComplexType(String name, List<GenericType> genericTypes, List<TsType> superTypes, Map<String, TsType> fields, Map<String, FunctionType> methods) {
        this.name = name;
        this.genericTypes = genericTypes;
        this.superTypes = superTypes;
        this.fields = fields;
        this.methods = methods;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    public String getTypeDeclaration() {
        String genericTypesString = genericTypes.stream().
                map(TsType::getTypeName)
                .collect(Collectors.joining(", "));
        if (!genericTypesString.isEmpty()) {
            genericTypesString = "<" + genericTypesString + ">";
        }

        String superTypesString = superTypes.stream().
                map(TsType::getTypeName)
                .collect(Collectors.joining(", "));
        if (!superTypesString.isEmpty()) {
            superTypesString = " extends " + superTypesString;
        }

        String fieldsString = fields.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().getNullableTypeName())
                .collect(Collectors.joining(INDENTED_NEW_LINE));

        String methodsString = methods.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().getTypeName())
                .collect(Collectors.joining(INDENTED_NEW_LINE));

        String body = Stream.of(fieldsString, methodsString)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(INDENTED_NEW_LINE));

        return """
                export declare interface %s%s%s {
                    %s
                }"""
                .formatted(name, genericTypesString, superTypesString, body);
    }
}
