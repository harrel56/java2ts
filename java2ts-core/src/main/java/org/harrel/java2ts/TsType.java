package org.harrel.java2ts;

interface TsType {

    default String getNullableTypeName() {
        return getTypeName() + " | null";
    }

    String getTypeName();

}
