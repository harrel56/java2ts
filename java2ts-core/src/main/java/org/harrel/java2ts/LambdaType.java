package org.harrel.java2ts;

import java.util.List;

abstract class LambdaType extends NamedType {
    LambdaType(String name, List<TsType> genericTypes) {
        super(name, genericTypes);
    }

    @Override
    public final String getTypeDeclaration() {
        return "export declare type %s%s = %s".formatted(name, getGenericTypesString(), getLambdaDefinition());
    }

    abstract String getLambdaDefinition();
}

class BaseLambdaType extends LambdaType {

    private final Holder<FunctionType> functionType;

    public BaseLambdaType(String name, List<TsType> genericTypes, Holder<FunctionType> functionType) {
        super(name, genericTypes);
        this.functionType = functionType;
    }

    @Override
    String getLambdaDefinition() {
        return functionType.getValue().getTypeNameAsLambda();
    }
}

class SubLambdaType extends LambdaType {

    private final TsType parent;

    public SubLambdaType(String name, List<TsType> genericTypes, TsType parent) {
        super(name, genericTypes);
        this.parent = parent;
    }

    @Override
    String getLambdaDefinition() {
        return parent.getTypeName();
    }
}
