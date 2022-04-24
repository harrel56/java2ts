package org.harrel.java2ts;

import java.util.List;
import java.util.stream.Collectors;

public class ParamType implements TsType {

    private final TsType rawType;
    private final List<GenericType> genericTypes;

    public ParamType(TsType rawType, List<GenericType> genericTypes) {
        this.rawType = rawType;
        this.genericTypes = genericTypes;
    }

    @Override
    public String getTypeName() {
        String genericTypesString = genericTypes.stream().
                map(TsType::getTypeName)
                .collect(Collectors.joining(", "));
        if(!genericTypesString.isEmpty()) {
            genericTypesString = "<" + genericTypesString + ">";
        }
        return rawType.getTypeName() + genericTypesString;
    }
}
