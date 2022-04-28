package org.harrel.java2ts;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ComplexType implements TsType {

    private static final String INDENTED_NEW_LINE = "\n    ";

    private final String name;
    private final List<TsType> genericTypes;
    private final List<TsType> superTypes;
    private final List<NamedProperty> fields;
    private final List<NamedProperty> methods;

    public ComplexType(String name, List<TsType> genericTypes, List<TsType> superTypes, List<NamedProperty> fields, List<NamedProperty> methods) {
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
        String genericTypesString = genericTypes.stream()
                .map(GenericType.class::cast)
                .map(GenericType::getBoundedTypeName)
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

        String fieldsString = fields.stream()
                .map(Object::toString)
                .collect(Collectors.joining(INDENTED_NEW_LINE));

        String methodsString = methods.stream()
                .map(Object::toString)
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
