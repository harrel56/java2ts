package org.harrel.java2ts;

public class ArrayType implements TsType {

    private final TsType elementType;

    public ArrayType(TsType elementType) {
        this.elementType = elementType;
    }

    @Override
    public String getTypeName() {
        return elementType.getTypeName() + "[]";
    }

}
