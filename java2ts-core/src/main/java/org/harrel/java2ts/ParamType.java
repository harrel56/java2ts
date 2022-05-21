package org.harrel.java2ts;

import java.util.List;
import java.util.stream.Collectors;

class ParamType implements TsType {

    private final TsType rawType;
    private final List<TsType> genericTypes;

    public ParamType(TsType rawType, List<TsType> genericTypes) {
        this.rawType = rawType;
        this.genericTypes = genericTypes;
    }

    @Override
    public String getTypeName() {
        String genericTypesString = genericTypes.stream()
                .map(TsType::getTypeName)
                .collect(Collectors.joining(", ", "<", ">"));

        if(rawType instanceof NamedType nType) {
            return nType.getRawTypeName() + genericTypesString;
        } else {
            return rawType.getTypeName() + genericTypesString;
        }
    }

    public boolean containsLambdaType() {
        return rawType instanceof LambdaType;
    }
}
