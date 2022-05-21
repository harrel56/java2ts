package org.harrel.java2ts;

import java.util.List;

class LambdaType extends NamedType {

    private final Holder<FunctionType> functionType;

    public LambdaType(String name, List<TsType> genericTypes, Holder<FunctionType> functionType) {
        super(name, genericTypes);
        this.functionType = functionType;
    }

    @Override
    public String getTypeDeclaration() {
        return "export declare type %s%s = %s".formatted(name, getGenericTypesString(), functionType.getValue().getTypeNameAsLambda());
    }

}
